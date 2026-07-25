from dataclasses import dataclass


@dataclass
class Song:
    uri: str
    name: str
    artists: list[str]
    year: int

    def __str__(self) -> str:
        return f"""
        URI: {self.uri}
        SONG NAME: {self.name}
        ARTISTS: {", ".join([artist for artist in self.artists])}
        YEAR: {self.year}
        """

def process_playlist_items(items: list[dict]) -> list[Song]:
    return [
        Song(
            uri=item["item"]["uri"],
            name=item["item"]["name"],
            artists=[artist["name"] for artist in item["item"]["artists"]],
            year=int(_get_year_from_release_date(item["item"]["album"]["release_date"]))
        ) 
        for item in items
    ]

def _get_year_from_release_date(date: str):
    return date.split('-')[0]


@dataclass
class PlaylistInfo:
    id: str
    name: str
    images: list[dict]


def process_playlist_infos(playlists: list[dict]) -> list[PlaylistInfo]:
    return [
        PlaylistInfo(
            id=playlist["id"],
            name=playlist["name"],
            images=playlist.get("images") or [],
        )
        for playlist in playlists
    ]