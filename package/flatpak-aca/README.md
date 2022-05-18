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
	
	# Run in background
	flatpak run org.hirs.aca

	# Run with bash interactive
	flatpak run --devel --command=/usr/bin/bash org.hirs.aca


# Statistics
	1st build of flatpak-aca folder ~ 580M
	1st build took 9.49 minutes
	Mariadb minimal custom shrunk from 926.2 MB to ~80mb

	tomcat resides in /var due to read only on al other folders in root
	mariadb and hirs are located in /apps

# Folder structures
	/app
		/hirs
			/certificates
			/scripts
			/config
			/wars
	/app
		/mariadb

	/var
		/tomcat


