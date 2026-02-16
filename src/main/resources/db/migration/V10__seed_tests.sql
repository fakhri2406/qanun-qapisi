-- Migration: Seed tests data from old .NET backend export
-- Source: Tests.csv (19 rows)
-- created_by is set dynamically to the first ADMIN user

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'c420ff29-8f4a-4990-ac96-412f5465795c',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Dövlət rəmzlərinə dair qanunvericilik',
    'Dövlət rəmzlərinə dair qanunvericiliyə aid 25
test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    25,
    25,
    '2025-10-07 12:16:35.871922',
    '2025-10-07 12:16:35.871922',
    '2025-10-07 12:16:35.871922'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'd629d11c-1be6-4741-973c-bfec0f29065e',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'İnzibati icraat haqqında Azərbaycan Respublikasının Qanunu',
    'BA, AC və AB kateqoriyaları üzrə İnzibati icraat haqqında Azərbaycan Respublikasının Qanununa aid 90 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    90,
    90,
    '2025-10-08 13:12:23.123224',
    '2025-10-08 13:12:23.123224',
    '2025-10-08 13:12:23.123224'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '69cefc7e-7ae2-4b63-b36b-fc2baca9de98',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Korrupsiyaya qarşı mübarizə haqqında Azərbaycan Respublikasının Qanunu',
    'Korrupsiyaya qarşı mübarizə haqqında Qanuna aid 50 test — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-09-28 12:52:58.753695',
    '2025-09-28 12:52:58.753695',
    '2025-09-28 12:52:58.753695'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'a2d307ef-96df-478d-80af-2c7aa06961ce',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'İnformasiya əldə etmək haqqında Azərbaycan Respublikasının qanunu',
    'AC və AB kateqoriyaları üzrə İnformasiya əldə etmək haqqında qanuna aid 50 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-01 15:54:55.484498',
    '2025-10-01 15:54:55.484498',
    '2025-10-01 15:54:55.484498'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '45d02d9d-0cda-4f57-8166-eb7a34f9f964',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Azərbaycan Respublikasının Konstitusiyası',
    'Konstitusiyaya aid 255 sual — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    255,
    255,
    '2025-10-04 13:45:59.235991',
    '2025-10-04 13:45:59.235991',
    '2025-10-04 13:45:59.235991'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '8e02f242-4572-49c2-bba0-df1d97b946e7',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Dövlət sirri haqqında Azərbaycan Respublikasının qanunu',
    'AC və AB kateqoriyaları üzrə Dövlət sirri haqqında Azərbaycan Respublikasının qanununa aid 50 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-09 12:57:08.632649',
    '2025-10-09 12:57:08.632649',
    '2025-10-09 12:57:08.632649'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'ba8f894a-71e4-4d9d-af15-da9a4dba44f5',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Dövlət qulluqçularının etik davranış qaydaları haqqında Azərbaycan Respublikasının Qanunu',
    'Dövlət qulluqçularının etik davranış qaydaları haqqında Qanuna aid 50 test — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-09-27 09:39:46.16585',
    '2025-09-27 09:39:46.16585',
    '2025-09-27 09:39:46.16585'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '4614a359-6770-44d8-a6f8-2533d961b7aa',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Azərbaycan Respublikasının Əmək Məcəlləsi  ',
    'AC kateqoriyası üzrə Əmək Məcəlləsinə aid 50 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-10 08:52:33.354645',
    '2025-10-10 08:52:33.354645',
    '2025-10-10 08:52:33.354645'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '9a653e20-e571-4419-b500-00296b3edcc5',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Elektron imza və elektron sənəd haqqında Azərbatəycan Respublikasının qanunu',
    'Elektron imza və elektron sənəd haqqında Azərbatəycan Respublikasının qanununa aid 30 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    30,
    30,
    '2025-10-02 15:01:38.877895',
    '2025-10-02 15:01:38.877895',
    '2025-10-02 15:01:38.877895'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '5532ba19-c482-44c4-b259-b0a84aeacbbb',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Azərbaycan Respublikasında dövlət dili haqqında Azərbaycan Respublikasının Qanunu',
    'Azərbaycan Respublikasında dövlət dili haqqında Azərbaycan Respublikasının Qanuna aid 25 test— imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    25,
    25,
    '2025-10-01 13:45:29.087022',
    '2025-10-01 13:45:29.087022',
    '2025-10-01 13:45:29.087022'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '2d1c16e0-30b2-4a89-a944-fc5a3869cfe6',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Normativ hüquqi aktlar haqqında Azərbaycan Respublikasinin Konstitusiya Qanunu',
    'Normativ hüquqi aktlar haqqında Konstitusiya Qanununa aid 30 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    30,
    30,
    '2025-10-08 09:45:26.576606',
    '2025-10-08 09:45:26.576606',
    '2025-10-08 09:45:26.576606'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '8604b35c-a756-4a54-85a6-723c728ad1f8',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Dövlət orqanlarında, dövlət mülkiyyətində olan və paylarının (səhmlərinin) nəzarət zərfi dövlətə məxsus olan hüquqi şəxslərdə və büdcə təşkilatlarında kargüzarlığın aparılmasına dair Təlimat',
    'Dövlət orqanlarında, dövlət mülkiyyətində olan və paylarının (səhmlərinin) nəzarət zərfi dövlətə məxsus olan hüquqi şəxslərdə və büdcə təşkilatlarında kargüzarlığın aparılmasına dair Təlimata aid 50 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-10 13:59:36.485457',
    '2025-10-10 13:59:36.485457',
    '2025-10-10 13:59:36.485457'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '77f40683-4fbb-4c27-abce-05b8830a195c',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Məhkəmələr və hakimlər haqqında Azərbaycan Respublikasının Qanunu',
    'Məhkəmələr və hakimlər haqqında qanuna aid 25 test— imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    25,
    25,
    '2025-09-28 16:38:49.275607',
    '2025-09-28 16:38:49.275607',
    '2025-09-28 16:38:49.275607'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'dd85ce7d-4358-4dc6-9b05-9464ac8ff1eb',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'İnzibati və yardımçı vəzifələrin təsnifat toplusu',
    'İnzibati və yardımçı vəzifələrin təsnifat toplusuna aid 20 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    20,
    20,
    '2025-10-10 18:17:50.161542',
    '2025-10-10 18:17:50.161542',
    '2025-10-10 18:17:50.161542'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '2b03908e-7d00-477d-80ef-efeaff9074db',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Vətəndaşların müraciətləri haqqında Azərbaycan Respublikasının qanunu',
    'Vətəndaşların müraciətləri haqqında qanuna aid 50 test — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-01 10:05:48.559397',
    '2025-10-01 10:05:48.559397',
    '2025-10-01 10:05:48.559397'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    'eb5c9df4-ae8f-43dd-ad53-fc0def743537',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Dövlət qulluğu haqqında Azərbaycan Respublikasının Qanunu',
    'Dövlət qulluğu haqqında Azərbaycan Respublikasının Qanunu aid 100 sual — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    100,
    100,
    '2025-09-24 19:19:30.496548',
    '2025-09-24 19:19:30.496548',
    '2025-09-24 19:19:30.496548'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '953731c5-1f0a-420e-b886-ce04bfbbba5a',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Azərbaycan Respublikasının Mülki Məcəlləsi',
    'AC kateqoriyası üzrə Mülki Məcəlləyə aid 50 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    50,
    50,
    '2025-10-08 08:17:18.79798',
    '2025-10-08 08:17:18.79798',
    '2025-10-08 08:17:18.79798'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '6f98fb22-d08d-4f5f-8ec8-465881f070a6',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Məlumat azadlığı haqqında Azərbaycan Respublikasının qanunu',
    'Məlumat azadlığı haqqında qanuna aid 25 test — imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    25,
    25,
    '2025-10-01 08:41:15.050673',
    '2025-10-01 08:41:15.050673',
    '2025-10-01 08:41:15.050673'
);

INSERT INTO tests (id, created_by, title, description, is_premium, status, question_count, total_possible_score, published_at, created_at, updated_at)
VALUES (
    '23909ea2-7a26-407b-a2ea-522563c649d3',
    (SELECT u.id FROM users u JOIN roles r ON u.role_id = r.id WHERE r.title = 'ADMIN' LIMIT 1),
    'Azərbaycan Respublikasının İnsan hüquqları üzrə müvəkkili (ombudsman) haqqında Azərbaycan Respublikasinin Konstitusiya Qanunu',
    'İnsan hüquqları üzrə müvəkkili (ombudsman) haqqında Azərbaycan Respublikasinin Konstitusiya Qanununa aid 25 test - imtahan standartlarına uyğun və situasiya tipli tapşırıqlarla',
    TRUE,
    'PUBLISHED',
    25,
    25,
    '2025-10-09 21:16:35.307512',
    '2025-10-09 21:16:35.307512',
    '2025-10-09 21:16:35.307512'
);
