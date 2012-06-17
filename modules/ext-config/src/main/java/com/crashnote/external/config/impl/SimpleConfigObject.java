/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.crashnote.external.config.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crashnote.external.config.ConfigException;
import com.crashnote.external.config.ConfigObject;
import com.crashnote.external.config.ConfigOrigin;
import com.crashnote.external.config.ConfigRenderOptions;
import com.crashnote.external.config.ConfigValue;

final class SimpleConfigObject extends AbstractConfigObject implements Serializable {

    private static final long serialVersionUID = 2L;

    // this map should never be modified - assume immutable
    final private Map<String, AbstractConfigValue> value;
    final private boolean resolved;
    final private boolean ignoresFallbacks;

    SimpleConfigObject(final ConfigOrigin origin,
            final Map<String, AbstractConfigValue> value, final ResolveStatus status,
            final boolean ignoresFallbacks) {
        super(origin);
        if (value == null)
            throw new ConfigException.BugOrBroken(
                    "creating config object with null map");
        this.value = value;
        this.resolved = status == ResolveStatus.RESOLVED;
        this.ignoresFallbacks = ignoresFallbacks;

        // Kind of an expensive debug check. Comment out?
        if (status != ResolveStatus.fromValues(value.values()))
            throw new ConfigException.BugOrBroken("Wrong resolved status on " + this);
    }

    SimpleConfigObject(final ConfigOrigin origin,
            final Map<String, AbstractConfigValue> value) {
        this(origin, value, ResolveStatus.fromValues(value.values()), false /* ignoresFallbacks */);
    }

    @Override
    public SimpleConfigObject withOnlyKey(final String key) {
        return withOnlyPath(Path.newKey(key));
    }

    @Override
    public SimpleConfigObject withoutKey(final String key) {
        return withoutPath(Path.newKey(key));
    }

    // gets the object with only the path if the path
    // exists, otherwise null if it doesn't. this ensures
    // that if we have { a : { b : 42 } } and do
    // withOnlyPath("a.b.c") that we don't keep an empty
    // "a" object.
    @Override
    protected SimpleConfigObject withOnlyPathOrNull(final Path path) {
        final String key = path.first();
        final Path next = path.remainder();
        AbstractConfigValue v = value.get(key);

        if (next != null) {
            if (v != null && (v instanceof AbstractConfigObject)) {
                v = ((AbstractConfigObject) v).withOnlyPathOrNull(next);
            } else {
                // if the path has more elements but we don't have an object,
                // then the rest of the path does not exist.
                v = null;
            }
        }

        if (v == null) {
            return null;
        } else {
            return new SimpleConfigObject(origin(), Collections.singletonMap(key, v),
                    v.resolveStatus(), ignoresFallbacks);
        }
    }

    @Override
    SimpleConfigObject withOnlyPath(final Path path) {
        final SimpleConfigObject o = withOnlyPathOrNull(path);
        if (o == null) {
            return new SimpleConfigObject(origin(),
                    Collections.<String, AbstractConfigValue> emptyMap(), ResolveStatus.RESOLVED,
                    ignoresFallbacks);
        } else {
            return o;
        }
    }

    @Override
    SimpleConfigObject withoutPath(final Path path) {
        final String key = path.first();
        final Path next = path.remainder();
        AbstractConfigValue v = value.get(key);

        if (v != null && next != null && v instanceof AbstractConfigObject) {
            v = ((AbstractConfigObject) v).withoutPath(next);
            final Map<String, AbstractConfigValue> updated = new HashMap<String, AbstractConfigValue>(
                    value);
            updated.put(key, v);
            return new SimpleConfigObject(origin(), updated, ResolveStatus.fromValues(updated
                    .values()), ignoresFallbacks);
        } else if (next != null || v == null) {
            // can't descend, nothing to remove
            return this;
        } else {
            final Map<String, AbstractConfigValue> smaller = new HashMap<String, AbstractConfigValue>(
                    value.size() - 1);
            for (final Map.Entry<String, AbstractConfigValue> old : value.entrySet()) {
                if (!old.getKey().equals(key))
                    smaller.put(old.getKey(), old.getValue());
            }
            return new SimpleConfigObject(origin(), smaller, ResolveStatus.fromValues(smaller
                    .values()), ignoresFallbacks);
        }
    }

    @Override
    protected AbstractConfigValue attemptPeekWithPartialResolve(final String key) {
        return value.get(key);
    }

    private SimpleConfigObject newCopy(final ResolveStatus newStatus, final ConfigOrigin newOrigin,
            final boolean newIgnoresFallbacks) {
        return new SimpleConfigObject(newOrigin, value, newStatus, newIgnoresFallbacks);
    }

    @Override
    protected SimpleConfigObject newCopy(final ResolveStatus newStatus, final ConfigOrigin newOrigin) {
        return newCopy(newStatus, newOrigin, ignoresFallbacks);
    }

    @Override
    protected SimpleConfigObject withFallbacksIgnored() {
        if (ignoresFallbacks)
            return this;
        else
            return newCopy(resolveStatus(), origin(), true /* ignoresFallbacks */);
    }

    @Override
    ResolveStatus resolveStatus() {
        return ResolveStatus.fromBoolean(resolved);
    }

    @Override
    protected boolean ignoresFallbacks() {
        return ignoresFallbacks;
    }

    @Override
    public Map<String, Object> unwrapped() {
        final Map<String, Object> m = new HashMap<String, Object>();
        for (final Map.Entry<String, AbstractConfigValue> e : value.entrySet()) {
            m.put(e.getKey(), e.getValue().unwrapped());
        }
        return m;
    }

    @Override
    protected SimpleConfigObject mergedWithObject(final AbstractConfigObject abstractFallback) {
        requireNotIgnoringFallbacks();

        if (!(abstractFallback instanceof SimpleConfigObject)) {
            throw new ConfigException.BugOrBroken(
                    "should not be reached (merging non-SimpleConfigObject)");
        }

        final SimpleConfigObject fallback = (SimpleConfigObject) abstractFallback;

        boolean changed = false;
        boolean allResolved = true;
        final Map<String, AbstractConfigValue> merged = new HashMap<String, AbstractConfigValue>();
        final Set<String> allKeys = new HashSet<String>();
        allKeys.addAll(this.keySet());
        allKeys.addAll(fallback.keySet());
        for (final String key : allKeys) {
            final AbstractConfigValue first = this.value.get(key);
            final AbstractConfigValue second = fallback.value.get(key);
            final AbstractConfigValue kept;
            if (first == null)
                kept = second;
            else if (second == null)
                kept = first;
            else
                kept = first.withFallback(second);

            merged.put(key, kept);

            if (first != kept)
                changed = true;

            if (kept.resolveStatus() == ResolveStatus.UNRESOLVED)
                allResolved = false;
        }

        final ResolveStatus newResolveStatus = ResolveStatus.fromBoolean(allResolved);
        final boolean newIgnoresFallbacks = fallback.ignoresFallbacks();

        if (changed)
            return new SimpleConfigObject(mergeOrigins(this, fallback), merged, newResolveStatus,
                    newIgnoresFallbacks);
        else if (newResolveStatus != resolveStatus() || newIgnoresFallbacks != ignoresFallbacks())
            return newCopy(newResolveStatus, origin(), newIgnoresFallbacks);
        else
            return this;
    }

    private SimpleConfigObject modify(final NoExceptionsModifier modifier) {
        try {
            return modifyMayThrow(modifier);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException.BugOrBroken("unexpected checked exception", e);
        }
    }

    private SimpleConfigObject modifyMayThrow(final Modifier modifier) throws Exception {
        Map<String, AbstractConfigValue> changes = null;
        for (final String k : keySet()) {
            final AbstractConfigValue v = value.get(k);
            // "modified" may be null, which means remove the child;
            // to do that we put null in the "changes" map.
            final AbstractConfigValue modified = modifier.modifyChildMayThrow(k, v);
            if (modified != v) {
                if (changes == null)
                    changes = new HashMap<String, AbstractConfigValue>();
                changes.put(k, modified);
            }
        }
        if (changes == null) {
            return this;
        } else {
            final Map<String, AbstractConfigValue> modified = new HashMap<String, AbstractConfigValue>();
            boolean sawUnresolved = false;
            for (final String k : keySet()) {
                if (changes.containsKey(k)) {
                    final AbstractConfigValue newValue = changes.get(k);
                    if (newValue != null) {
                        modified.put(k, newValue);
                        if (newValue.resolveStatus() == ResolveStatus.UNRESOLVED)
                            sawUnresolved = true;
                    } else {
                        // remove this child; don't put it in the new map.
                    }
                } else {
                    final AbstractConfigValue newValue = value.get(k);
                    modified.put(k, newValue);
                    if (newValue.resolveStatus() == ResolveStatus.UNRESOLVED)
                        sawUnresolved = true;
                }
            }
            return new SimpleConfigObject(origin(), modified,
                    sawUnresolved ? ResolveStatus.UNRESOLVED : ResolveStatus.RESOLVED,
                    ignoresFallbacks());
        }
    }

    @Override
    AbstractConfigObject resolveSubstitutions(final ResolveContext context) throws NotPossibleToResolve {
        if (resolveStatus() == ResolveStatus.RESOLVED)
            return this;

        try {
            return modifyMayThrow(new Modifier() {

                @Override
                public AbstractConfigValue modifyChildMayThrow(final String key, final AbstractConfigValue v)
                        throws NotPossibleToResolve {
                    if (context.isRestrictedToChild()) {
                        if (key.equals(context.restrictToChild().first())) {
                            final Path remainder = context.restrictToChild().remainder();
                            if (remainder != null) {
                                return context.restrict(remainder).resolve(v);
                            } else {
                                // we don't want to resolve the leaf child.
                                return v;
                            }
                        } else {
                            // not in the restrictToChild path
                            return v;
                        }
                    } else {
                        // no restrictToChild, resolve everything
                        return context.unrestricted().resolve(v);
                    }
                }

            });
        } catch (NotPossibleToResolve e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException.BugOrBroken("unexpected checked exception", e);
        }
    }

    @Override
    SimpleConfigObject relativized(final Path prefix) {
        return modify(new NoExceptionsModifier() {

            @Override
            public AbstractConfigValue modifyChild(final String key, final AbstractConfigValue v) {
                return v.relativized(prefix);
            }

        });
    }

    @Override
    protected void render(final StringBuilder sb, final int indent, final ConfigRenderOptions options) {
        if (isEmpty()) {
            sb.append("{}");
        } else {
            sb.append("{");
            if (options.getFormatted())
                sb.append('\n');
            for (final String k : keySet()) {
                final AbstractConfigValue v;
                v = value.get(k);

                if (options.getOriginComments()) {
                    indent(sb, indent + 1, options);
                    sb.append("# ");
                    sb.append(v.origin().description());
                    sb.append("\n");
                }
                if (options.getComments()) {
                    for (final String comment : v.origin().comments()) {
                        indent(sb, indent + 1, options);
                        sb.append("# ");
                        sb.append(comment);
                        sb.append("\n");
                    }
                }
                indent(sb, indent + 1, options);
                v.render(sb, indent + 1, k, options);
                sb.append(",");
                if (options.getFormatted())
                    sb.append('\n');
            }
            // chop comma or newline
            sb.setLength(sb.length() - 1);
            if (options.getFormatted()) {
                sb.setLength(sb.length() - 1); // also chop comma
                sb.append("\n"); // put a newline back
                indent(sb, indent, options);
            }
            sb.append("}");
        }
    }

    @Override
    public AbstractConfigValue get(final Object key) {
        return value.get(key);
    }

    private static boolean mapEquals(final Map<String, ConfigValue> a, final Map<String, ConfigValue> b) {
        final Set<String> aKeys = a.keySet();
        final Set<String> bKeys = b.keySet();

        if (!aKeys.equals(bKeys))
            return false;

        for (final String key : aKeys) {
            if (!a.get(key).equals(b.get(key)))
                return false;
        }
        return true;
    }

    private static int mapHash(final Map<String, ConfigValue> m) {
        // the keys have to be sorted, otherwise we could be equal
        // to another map but have a different hashcode.
        final List<String> keys = new ArrayList<String>();
        keys.addAll(m.keySet());
        Collections.sort(keys);

        int valuesHash = 0;
        for (final String k : keys) {
            valuesHash += m.get(k).hashCode();
        }
        return 41 * (41 + keys.hashCode()) + valuesHash;
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof ConfigObject;
    }

    @Override
    public boolean equals(final Object other) {
        // note that "origin" is deliberately NOT part of equality.
        // neither are other "extras" like ignoresFallbacks or resolve status.
        if (other instanceof ConfigObject) {
            // optimization to avoid unwrapped() for two ConfigObject,
            // which is what AbstractConfigValue does.
            return canEqual(other) && mapEquals(this, ((ConfigObject) other));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // note that "origin" is deliberately NOT part of equality
        // neither are other "extras" like ignoresFallbacks or resolve status.
        return mapHash(this);
    }

    @Override
    public boolean containsKey(final Object key) {
        return value.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return value.keySet();
    }

    @Override
    public boolean containsValue(final Object v) {
        return value.containsValue(v);
    }

    @Override
    public Set<Map.Entry<String, ConfigValue>> entrySet() {
        // total bloat just to work around lack of type variance

        final HashSet<java.util.Map.Entry<String, ConfigValue>> entries = new HashSet<Map.Entry<String, ConfigValue>>();
        for (final Map.Entry<String, AbstractConfigValue> e : value.entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<String, ConfigValue>(
                    e.getKey(), e
                    .getValue()));
        }
        return entries;
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public Collection<ConfigValue> values() {
        return new HashSet<ConfigValue>(value.values());
    }

    final private static String EMPTY_NAME = "empty config";
    final private static SimpleConfigObject emptyInstance = empty(SimpleConfigOrigin
            .newSimple(EMPTY_NAME));

    final static SimpleConfigObject empty() {
        return emptyInstance;
    }

    final static SimpleConfigObject empty(final ConfigOrigin origin) {
        if (origin == null)
            return empty();
        else
            return new SimpleConfigObject(origin,
                    Collections.<String, AbstractConfigValue> emptyMap());
    }

    final static SimpleConfigObject emptyMissing(final ConfigOrigin baseOrigin) {
        return new SimpleConfigObject(SimpleConfigOrigin.newSimple(
                baseOrigin.description() + " (not found)"),
                Collections.<String, AbstractConfigValue> emptyMap());
    }

    // serialization all goes through SerializedConfigValue
    private Object writeReplace() throws ObjectStreamException {
        return new SerializedConfigValue(this);
    }
}