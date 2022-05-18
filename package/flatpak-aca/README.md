# Install flatpak
	https://flatpak.org/setup/

# Quick Tutorial
	https://docs.flatpak.org/en/latest/getting-started.html

# Extensions to install
	Runtime to choose is 20.08
	flatpak install org.freedesktop.Sdk.Extension.openjdk8


# Commands for installing building flatpak app
	flatpak-builder --user --install --force-clean build-dir org.hirs.aca.yml

# Command for running flatpak app

	flatpak run org.hirs.aca
	flatpak run --devel --command=/usr/bin/bash org.hirs.aca
