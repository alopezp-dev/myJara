INSERT INTO users (username, password, full_name, role)
VALUES (
           'admin',
           '$2a$10$eHuOehxHUvewu07Z8y3theAsrIKXNHWyA8KHiK4xZ0aNz34kUrcKi',
           'Administrador MyJara',
           'ADMIN'
       ) ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, full_name, role)
VALUES (
           'mlopez',
           '$2a$10$HdNXpbLuvd6at6SRsHSsCedbctelJsIeJwvqsTBrjeFoE0rqUY2Je',
           'Maria Lopez',
           'MEDICO'
       ) ON CONFLICT (username) DO NOTHING;