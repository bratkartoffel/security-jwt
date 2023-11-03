#!/bin/bash
set -e

current_version=$(cat version.txt)
release_version=${current_version/-SNAPSHOT/}
previous_version=$(grep -Eo '^\[[0-9.]+\]' CHANGELOG.md | tr -d '[]' | sort -V | tail -n1)
next_version="$(perl -pe 's/^((\d+\.)*)(\d+)(.*)$/$1.($3+1).$4/e' <<<"$release_version")-SNAPSHOT"

# start release
git flow release start "$release_version"

# set version number to release
sed -i "s/$current_version/$release_version/" version.txt
git add version.txt
git commit -m "Version updated from $current_version to version $release_version"

# update the changelog
sed -i \
  -e "/### \[unreleased/a - no changes yet\n\n### [$release_version] ($(date +%Y-%m-%d))" \
  -e "/^\[unreleased\]:/a [$release_version]: https://github.com/bratkartoffel/security-jwt/compare/${previous_version}...${release_version}" \
  -e "s|^\[unreleased\]:.\+develop$|[unreleased]: https://github.com/bratkartoffel/security-jwt/compare/${release_version}...develop|" \
  CHANGELOG.md

# commit the changelog
git add CHANGELOG.md
git commit -m "Add release $release_version to changelog"

# update the READMEs
find . -name README.md -exec sed -i "s@<version>${previous_version}</version>@<version>${release_version}</version>@" {} \;
find . -name README.md -exec git add {} \;
git commit -m "Update README with new release"

# run the build and publish to central
./gradlew clean publish

# finish the release, merge the branches and create tag
git flow release finish -m "Release $release_version"

# set version number to next snapshot
sed -i "s/$release_version/$next_version/" version.txt
git add version.txt
git commit -m "Update version to next snapshot"

# push everything upstream
git push --atomic origin -- develop master refs/tags/"$release_version"
