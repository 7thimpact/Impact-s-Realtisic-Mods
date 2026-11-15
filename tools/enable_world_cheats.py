"""Turns cheats on in a world's level.dat, so chat commands work in scripted client runs.

level.dat is gzipped NBT. The allowCommands flag is a byte tag, so this finds the tag by its name and sets its value,
without parsing the rest of the file.
"""
import gzip
import sys
from pathlib import Path

TAG_BYTE = 1
TAG_NAME = b"allowCommands"
CHEATS_ON = 1

level_file = Path(sys.argv[1])
data = bytearray(gzip.decompress(level_file.read_bytes()))
tag_header = bytes([TAG_BYTE]) + len(TAG_NAME).to_bytes(2, "big") + TAG_NAME
position = data.find(tag_header)
if position < 0:
    sys.exit(f"No allowCommands tag in {level_file}")
data[position + len(tag_header)] = CHEATS_ON
level_file.write_bytes(gzip.compress(bytes(data)))
print(f"Cheats on in {level_file}")
