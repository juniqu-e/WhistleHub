-- 외래 키 제약 조건 비활성화
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS whistlehub.reported_layer;
DROP TABLE IF EXISTS whistlehub.report;
DROP TABLE IF EXISTS whistlehub.playlist_track;
DROP TABLE IF EXISTS whistlehub.listen_record;
DROP TABLE IF EXISTS whistlehub.`like`;
DROP TABLE IF EXISTS whistlehub.hate;
DROP TABLE IF EXISTS whistlehub.comment;
DROP TABLE IF EXISTS whistlehub.layer;
DROP TABLE IF EXISTS whistlehub.sampling;
DROP TABLE IF EXISTS whistlehub.track_tag;
DROP TABLE IF EXISTS whistlehub.track;
DROP TABLE IF EXISTS whistlehub.follow;
DROP TABLE IF EXISTS whistlehub.playlist;
DROP TABLE IF EXISTS whistlehub.tag;
DROP TABLE IF EXISTS whistlehub.member;
DROP TABLE IF EXISTS whistlehub.layer_file;

-- 외래 키 제약 조건 재활성화
SET FOREIGN_KEY_CHECKS = 1;


create table whistlehub.tag
(
    tag_id     int auto_increment
        primary key,
    created_at varchar(255) null,
    name       varchar(255) null,
    updated_at varchar(255) null
);

INSERT INTO whistlehub.tag (tag_id, name) VALUES
                                              (1, 'Classical'),
                                              (2, 'Jazz'),
                                              (3, 'CCM'),
                                              (4, 'Pop'),
                                              (5, 'Ballad'),
                                              (6, 'R&B'),
                                              (7, 'Hip Hop'),
                                              (8, 'Country'),
                                              (9, 'Folk'),
                                              (10, 'Reggae'),
                                              (11, 'Disco'),
                                              (12, 'Rock'),
                                              (13, 'EDM'),
                                              (14, 'Trot'),
                                              (15, 'Musical'),
                                              (16, 'Whistle'),
                                              (17, 'Acoustic Guitar'),
                                              (18, 'Voice'),
                                              (19, 'Drums'),
                                              (20, 'Bass'),
                                              (21, 'Electric Guitar'),
                                              (22, 'Piano'),
                                              (23, 'Anger'),
                                              (24, 'Sadness'),
                                              (25, 'Depression'),
                                              (26, 'Joy'),
                                              (27, 'Happiness'),
                                              (28, 'Confusion'),
                                              (29, 'Comfort'),
                                              (30, 'Excitement'),
                                              (31, 'Nostalgia'),
                                              (32, 'Anxiety'),
                                              (33, 'Loneliness'),
                                              (34, 'Calmness'),
                                              (35, 'Study'),
                                              (36, 'Chill'),
                                              (37, 'Reading'),
                                              (38, 'Drive'),
                                              (39, 'Meditation'),
                                              (40, 'Workout'),
                                              (41, 'Shower'),
                                              (42, 'Healing'),
                                              (43, 'Coffee'),
                                              (44, 'Travel'),
                                              (45, 'Work');
