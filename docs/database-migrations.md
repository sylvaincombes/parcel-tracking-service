# Database migrations

Schema changes are Liquibase changesets in `src/main/resources/db/changelog/changes/`, one file per changeset, listed
explicitly in `db.changelog-master.yaml`. They run at application start-up, before the new version receives traffic,
while the previous version still serves requests on the same database.

## Naming

`<yyyy-mm-dd>-<what-it-does>.yaml`, with the changeset `id` equal to the file name without extension and `author`
set to your handle.

## Lifecycle of a changeset

- Feature branches are never deployed to shared environments: only `main` is deployed (staging, then production).
  A changeset that is not on `main` has only ever run on developers' laptops and CI's throw-away database.
- While a changeset is not on `main`, edit it in place. Do not stack "fix" changesets on top of one that was never
  released. Reset your local database (`docker compose down -v`) after editing.
- Once a changeset is on `main`, it is immutable: Liquibase stores its checksum in every environment. Fix forward
  with a new changeset.
- Every changeset has an explicit `rollback` block that undoes exactly what the changeset does.

## Table sizes (production, November 2026)

| Table | Rows | Growth | Notes |
|---|---|---|---|
| `parcel` | ~38 M | ~140 k/day | read on every public tracking request |
| `tracking_event` | ~410 M | ~1.3 M/day | append-only, written by every carrier webhook |
| `outbox_message` | < 2 M | purged after 7 days | |

## Rules for `parcel` and `tracking_event`

Both tables are written continuously by carrier webhooks. A lock held for more than a couple of seconds makes carriers
time out and retry, which piles up.

- Adding a nullable column, or a column with a constant default, is fine (metadata-only in PostgreSQL 16).
- No data backfill (`UPDATE` over existing rows) in a changeset. Backfills run as a batched job (`jobs/` runbook in the
  team wiki), in chunks of at most 10 000 rows, after the column exists.
- `NOT NULL` on an existing column is a three-step change across releases: add the column nullable, backfill, then add
  `CHECK (column IS NOT NULL) NOT VALID` followed by `VALIDATE CONSTRAINT` in a later changeset.
- Indexes are created with `CREATE INDEX CONCURRENTLY` in their own changeset with `runInTransaction: false`
  (see `2026-07-20-index-tracking-event-parcel-occurred-at.yaml`).
- Unique constraints: build the unique index concurrently first, then `ALTER TABLE ... ADD CONSTRAINT ... UNIQUE USING
  INDEX ...` in a separate changeset.

Small tables (`outbox_message`, new tables) do not need these precautions.
