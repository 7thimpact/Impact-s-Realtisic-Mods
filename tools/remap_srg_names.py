"""Replaces SRG names (m_12345_, f_12345_) in decompiled sources with readable names from a tsrg2 mapping file."""
import re
import sys
from pathlib import Path

mapping_path, source_root, output_root = (Path(argument) for argument in sys.argv[1:4])
readable_by_srg = {}
for line in mapping_path.read_text().splitlines():
    if not line.startswith("\t") or line.startswith("\t\t"):
        continue
    parts = line.strip().split(" ")
    readable, srg = parts[0], parts[-1]
    if re.fullmatch(r"[mf]_\d+_", srg):
        readable_by_srg[srg] = readable

srg_name = re.compile(r"\b[mf]_\d+_\b")
for source_file in source_root.rglob("*.java"):
    text = source_file.read_text()
    remapped = srg_name.sub(lambda match: readable_by_srg.get(match.group(0), match.group(0)), text)
    target = output_root / source_file.relative_to(source_root)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(remapped)
print(f"{len(readable_by_srg)} names mapped")
