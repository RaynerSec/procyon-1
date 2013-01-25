package com.strobel.assembler;

import com.strobel.core.IFreezable;
import com.strobel.core.VerifyArgument;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * @author Mike Strobel
 */
public class Collection<E> extends AbstractList<E> implements IFreezable {
    private final ArrayList<E> _items;
    private boolean _isFrozen;

    public Collection() {
        _items = new ArrayList<>();
    }

    @Override
    public final int size() {
        return _items.size();
    }

    @Override
    public final E get(final int index) {
        return _items.get(index);
    }

    @Override
    public final boolean add(final E e) {
        verifyNotFrozen();
        add(size(), e);
        return true;
    }

    @Override
    public final E set(final int index, final E element) {
        verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        beforeSet(index, element);
        _items.set(index, element);
        return super.set(index, element);
    }

    @Override
    public final void add(final int index, final E element) {
        verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        final boolean append = index == size();
        _items.add(index, element);
        afterAdd(index, element, append);
    }

    @Override
    public final E remove(final int index) {
        verifyNotFrozen();
        final E e = _items.remove(index);
        if (e != null) {
            afterRemove(index, e);
        }
        return e;
    }

    @Override
    public final void clear() {
        verifyNotFrozen();
        beforeClear();
        super.clear();
    }

    @Override
    public final boolean remove(final Object o) {
        verifyNotFrozen();

        @SuppressWarnings("SuspiciousMethodCalls")
        final int index = _items.indexOf(o);

        if (index < 0) {
            return false;
        }

        return remove(index) != null;
    }

    protected void afterAdd(final int index, final E e, final boolean appended) {}

    protected void beforeSet(final int index, final E e) {}

    protected void afterRemove(final int index, final E e) {}

    protected void beforeClear() {}

    // <editor-fold defaultstate="collapsed" desc="IFreezable Implementation">

    @Override
    public boolean canFreeze() {
        return !isFrozen();
    }

    @Override
    public final boolean isFrozen() {
        return _isFrozen;
    }

    @Override
    public final void freeze() {
        freeze(true);
    }

    public final void freeze(final boolean freezeContents) {
        if (!canFreeze()) {
            throw new IllegalStateException(
                "Collection cannot be frozen.  Be sure to check canFreeze() before calling " +
                "freeze(), or use the tryFreeze() method instead."
            );
        }

        freezeCore(freezeContents);

        _isFrozen = true;
    }

    protected void freezeCore(final boolean freezeContents) {
        if (freezeContents) {
            for (final E item : _items) {
                if (item instanceof IFreezable) {
                    ((IFreezable) item).freezeIfUnfrozen();
                }
            }
        }
    }

    protected final void verifyNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("Frozen collections cannot be modified.");
        }
    }

    protected final void verifyFrozen() {
        if (!isFrozen()) {
            throw new IllegalStateException(
                "Collection must be frozen before performing this operation."
            );
        }
    }

    @Override
    public final boolean tryFreeze() {
        if (!canFreeze()) {
            return false;
        }

        try {
            freeze();
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public final void freezeIfUnfrozen() throws IllegalStateException {
        if (isFrozen()) {
            return;
        }
        freeze();
    }

    // </editor-fold>
}