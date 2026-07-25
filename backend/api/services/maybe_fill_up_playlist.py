import random

from .helpers import Song

MIN_SONGS_PER_BUCKET = 2

# Year periods used to bucket songs. Every playlist song falls into exactly one
# period; years before the first period are clamped into it, years after the last
# into that one, so no real song is ever dropped.
PERIODS = [
    (1951, 1960),
    (1961, 1970),
    (1971, 1975),
    (1976, 1980),
    (1981, 1985),
    (1986, 1990),
    (1991, 1995),
    (1996, 2000),
    (2001, 2005),
    (2006, 2010),
    (2011, 2015),
    (2016, 2020),
    (2021, 2026),
]


def maybe_fill_up_playlist(
    playlist: list[Song], final_playlist_size: int, backup_playlist: list[Song]
) -> list[Song]:
    """Fill playlist with songs from backup playlist to reach `final_playlist_size`."""
    if len(playlist) >= final_playlist_size:
        return playlist

    return _fill_up_with_backup_songs(playlist, final_playlist_size, backup_playlist)


def _fill_up_with_backup_songs(
    playlist: list[Song], final_playlist_size: int, backup_playlist: list[Song]
) -> list[Song]:
    buckets = _songs_by_periods(playlist)
    buckets = _fill_buckets_to_sizes(
        buckets, backup_playlist, {period: MIN_SONGS_PER_BUCKET for period in buckets}
    )

    final_bucket_sizes = _calculate_final_bucket_sizes(buckets, final_playlist_size)

    buckets = _fill_buckets_to_sizes(buckets, backup_playlist, final_bucket_sizes)

    return _buckets_to_playlist(buckets)


def _calculate_final_bucket_sizes(
    buckets: dict[tuple[int, int], list[Song]], final_total_size: int
) -> dict[tuple[int, int], int]:
    """Decide how many songs each bucket should hold in the end, distributed
    proportionally to the buckets' current sizes so their relative weights are
    preserved. The returned sizes sum to `final_total_size` and are never below a
    bucket's current size (we only ever add songs)."""
    current_sizes = {period: len(songs) for period, songs in buckets.items()}
    current_total = sum(current_sizes.values())

    if final_total_size <= current_total:
        return current_sizes

    exact = {
        period: final_total_size * current_size / current_total
        for period, current_size in current_sizes.items()
    }
    # floor(ratio * c) >= c because ratio >= 1, so we never drop below current.
    final_sizes = {period: int(value) for period, value in exact.items()}

    remainder = final_total_size - sum(final_sizes.values())
    by_fraction = sorted(
        current_sizes,
        key=lambda period: exact[period] - final_sizes[period], reverse=True
    )
    for period in by_fraction[:remainder]:
        final_sizes[period] += 1

    return final_sizes


def _fill_buckets_to_sizes(
    buckets: dict[tuple[int, int], list[Song]],
    backup_playlist: list[Song],
    target_sizes: dict[tuple[int, int], int],
) -> dict[tuple[int, int], list[Song]]:
    """Fill each bucket with random period-matching backup songs until it reaches
    its target size (or the backup runs out of matching songs for that period).
    A bucket may stay below its target when the backup can't cover it."""
    backup_by_period = _songs_by_periods(backup_playlist)

    for period, songs in buckets.items():
        candidates = _fresh_candidates(songs, backup_by_period[period])
        while len(songs) < target_sizes[period] and candidates:
            songs.append(candidates.pop())

    return buckets


def _buckets_to_playlist(
    buckets: dict[tuple[int, int], list[Song]],
) -> list[Song]:
    return [song for songs in buckets.values() for song in songs]



def _songs_by_periods(
    playlist: list[Song],
) -> dict[tuple[int, int], list[Song]]:
    buckets: dict[tuple[int, int], list[Song]] = {period: [] for period in PERIODS}

    for song in playlist:
        buckets[_period_for_year(song.year)].append(song)
    return buckets


def _period_for_year(year: int) -> tuple[int, int]:
    """Map a year to its period, clamping out-of-range years to the nearest
    edge period so real songs are never lost."""
    for start, end in PERIODS:
        if year <= end:
            return (start, end)
    return PERIODS[-1]


def _fresh_candidates(existing: list[Song], new: list[Song]) -> list[Song]:
    """Backup songs for a period that aren't already in the bucket, shuffled so
    picking from the end draws a random song."""
    existing_uris = {song.uri for song in existing}
    candidates = [song for song in new if song.uri not in existing_uris]

    random.shuffle(candidates)
    return candidates
