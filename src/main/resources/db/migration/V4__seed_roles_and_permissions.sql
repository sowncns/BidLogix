-- Sample permissions covering typical ERP modules (users/roles, products, orders, warehouse, accounting).
INSERT INTO permissions (code, description) VALUES
    ('USER_VIEW',        'Xem danh sách người dùng'),
    ('USER_CREATE',      'Tạo người dùng'),
    ('USER_UPDATE',      'Cập nhật người dùng'),
    ('USER_DELETE',      'Xóa người dùng'),
    ('ROLE_MANAGE',      'Quản lý role và phân quyền'),

    ('PRODUCT_VIEW',     'Xem sản phẩm'),
    ('PRODUCT_CREATE',   'Tạo sản phẩm'),
    ('PRODUCT_UPDATE',   'Cập nhật sản phẩm'),
    ('PRODUCT_DELETE',   'Xóa sản phẩm'),

    ('ORDER_VIEW',       'Xem đơn hàng'),
    ('ORDER_CREATE',     'Tạo đơn hàng'),
    ('ORDER_UPDATE',     'Cập nhật đơn hàng'),
    ('ORDER_APPROVE',    'Duyệt đơn hàng'),
    ('ORDER_DELETE',     'Xóa đơn hàng'),

    ('WAREHOUSE_VIEW',   'Xem tồn kho'),
    ('WAREHOUSE_UPDATE', 'Cập nhật tồn kho'),

    ('ACCOUNTING_VIEW',  'Xem sổ sách kế toán'),
    ('ACCOUNTING_UPDATE','Cập nhật sổ sách kế toán')
ON CONFLICT (code) DO NOTHING;

-- Update sample role data scopes (roles ADMIN/STAFF/CUSTOMER already seeded in V1/V3).
UPDATE roles SET data_scope = 'ALL'          WHERE code = 'ADMIN';
UPDATE roles SET data_scope = 'ORGANIZATION' WHERE code = 'STAFF';
UPDATE roles SET data_scope = 'SELF'         WHERE code = 'CUSTOMER';

-- Two extra sample roles for a typical ERP org chart.
INSERT INTO roles (name, code, data_scope) VALUES
    ('Manager', 'MANAGER', 'ORGANIZATION_AND_CHILDREN'),
    ('Accountant', 'ACCOUNTANT', 'ORGANIZATION')
ON CONFLICT (code) DO NOTHING;

-- ADMIN: full access to every permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- MANAGER: everything except user/role administration and deletes.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MANAGER'
  AND p.code IN (
    'PRODUCT_VIEW','PRODUCT_CREATE','PRODUCT_UPDATE',
    'ORDER_VIEW','ORDER_CREATE','ORDER_UPDATE','ORDER_APPROVE',
    'WAREHOUSE_VIEW','WAREHOUSE_UPDATE',
    'ACCOUNTING_VIEW'
  )
ON CONFLICT DO NOTHING;

-- STAFF: day-to-day operational access, no approve/delete.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'STAFF'
  AND p.code IN ('PRODUCT_VIEW','ORDER_VIEW','ORDER_CREATE','ORDER_UPDATE','WAREHOUSE_VIEW')
ON CONFLICT DO NOTHING;

-- ACCOUNTANT: accounting + read-only order/product visibility.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ACCOUNTANT'
  AND p.code IN ('ACCOUNTING_VIEW','ACCOUNTING_UPDATE','ORDER_VIEW','PRODUCT_VIEW')
ON CONFLICT DO NOTHING;

-- CUSTOMER: self-service only.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'CUSTOMER'
  AND p.code IN ('ORDER_VIEW','ORDER_CREATE')
ON CONFLICT DO NOTHING;
