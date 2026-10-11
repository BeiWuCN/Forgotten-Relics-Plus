import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile


def gh(*arguments):
    return subprocess.check_output(["gh", *arguments], text=True, encoding="utf-8")


tag = os.environ["RELEASE_TAG"]
if not re.fullmatch(r"v\d+\.\d+\.\d+(?:[.-][A-Za-z0-9.-]+)?", tag):
    raise ValueError("Expected a version tag such as v1.7.1")
repository = os.environ.get("GITHUB_REPOSITORY", "BeiWuCN/Forgotten-Relics-Plus")
release = json.loads(gh("release", "view", tag, "--repo", repository,
                        "--json", "assets,body,name,isDraft,isPrerelease"))
if release["isDraft"]:
    raise ValueError("Only published releases can be synced")
version = tag.removeprefix("v")
filename = f"forgotten_relics_plus-1.21.1-neoforge-{version}.jar"
assets = [asset for asset in release["assets"] if asset["name"] == filename]
if len(assets) != 1 or assets[0]["state"] != "uploaded":
    raise ValueError("Expected one uploaded release mod JAR")
destination = Path("build/curseforge")
destination.mkdir(parents=True, exist_ok=True)
artifact = destination / filename
gh("release", "download", tag, "--repo", repository, "--pattern", filename,
   "--dir", str(destination), "--clobber")
digest = "sha256:" + hashlib.sha256(artifact.read_bytes()).hexdigest()
if assets[0].get("digest") != digest:
    raise ValueError("Downloaded release artifact failed SHA-256 validation")
with zipfile.ZipFile(artifact) as archive:
    metadata = archive.read("META-INF/neoforge.mods.toml").decode("utf-8")
    if not re.search(rf'^version\s*=\s*"{re.escape(version)}"', metadata, re.MULTILINE):
        raise ValueError("JAR version does not match release tag")
    if not re.search(r'^modId\s*=\s*"forgotten_relics_plus"', metadata, re.MULTILINE):
        raise ValueError("Unexpected mod in release artifact")
(destination / "changelog.md").write_text(release["body"], encoding="utf-8")
outputs = {"file": artifact.as_posix(), "name": release["name"],
           "version": version, "type": "beta" if release["isPrerelease"] else "release"}
if any("\n" in value or "\r" in value for value in outputs.values()):
    raise ValueError("Invalid multiline release metadata")
with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
    output.writelines(f"{key}={value}\n" for key, value in outputs.items())
print(f"Validated {filename}; ready to sync to CurseForge")
