package com.qs.Backend.platform.permission.entity;

// Ordered from least to most permissive - ordinal() drives "most permissive wins" comparisons.
public enum DataScope {
    SELF,
    ORGANIZATION,
    ORGANIZATION_AND_CHILDREN,
    ALL;

    public static DataScope mostPermissive(DataScope a, DataScope b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
