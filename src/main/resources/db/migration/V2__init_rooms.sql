CREATE TABLE rooms (
                       id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title            VARCHAR(100) NOT NULL,
                       description      TEXT,
                       battle_type      VARCHAR(20)  NOT NULL DEFAULT 'MUSIC',
                       status           VARCHAR(20)  NOT NULL DEFAULT 'WAITING',
                       creator_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       max_participants INTEGER      NOT NULL DEFAULT 8,
                       battle_config    JSONB        NOT NULL DEFAULT '{}'::jsonb,
                       version          BIGINT       NOT NULL DEFAULT 0,
                       created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                       updated_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE room_participants (
                                   id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   room_id    UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
                                   user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   joined_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                   CONSTRAINT uk_room_user UNIQUE (room_id, user_id)
);

CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_rooms_creator ON rooms(creator_id);
CREATE INDEX idx_room_participants_room ON room_participants(room_id);
