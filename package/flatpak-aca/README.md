# Install flatpak
	https://flatpak.org/setup/

# Quick Tutorial
	https://docs.flatpak.org/en/latest/getting-started.html

# Extensions to install
	Runtime to choose is 21.08
	flatpak install flathub org.freedesktop.Platform//21.08 org.freedesktop.Sdk//21.08
	flatpak install org.freedesktop.Sdk.Extension.openjdk8

# Commands for installing building flatpak app
	flatpak-builder --user --install --force-clean build-dir org.hirs.aca.yml

# Command for running flatpak app
	# Run in background
	flatpak run org.hirs.aca

	# Run with bash interactive
	flatpak run --devel --command=/usr/bin/bash org.hirs.aca


# Folder Structures for Build
	/app
		/mariadb
		/tomcat
		/hirs

# Folder Structures for runtime
	/var/tmp
		/hirs
			/aca
				/certificates
				/client-files
			/certificates
			/scripts
			/config
		/tomcat
		/mariadb
