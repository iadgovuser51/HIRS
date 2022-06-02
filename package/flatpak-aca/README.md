# Install flatpak
	https://flatpak.org/setup/

# Quick Tutorial
	https://docs.flatpak.org/en/latest/getting-started.html

# Runtimes/Extensions to install for building
	flatpak remote-add --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
	flatpak install flathub org.freedesktop.Platform//21.08 org.freedesktop.Sdk//21.08
	flatpak install org.freedesktop.Sdk.Extension.openjdk8//21.08

# Commands for installing/building flatpak app
	flatpak-builder --user --install --force-clean build-dir org.hirs.aca.yml

# Command for running flatpak app
	# Run in background
	flatpak run org.hirs.aca

	# Run with bash
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


# Repo Steps

## From inside the folder where the manifests resides (org.hirs.aca.yml), run the following command to put the flatpak app in a local repo

	flatpak-builder --repo=/some/dir/flatpak-repo/ --user --install --force-clean build-dir org.hirs.aca.yml

## Note:
	The --repo=/path/folder flat points to a directory where you want a repo created and the flatpak app stored.
	Then upload the repo folder

## Exporting a flatpak build-bundle for install 
	flatpak build-bundle /some/dir/flatpak-repo/ hirs.aca.flatpak org.hirs.aca

## Flatpak runtimes and extension dependencies for aca
	flatpak remote-add --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
	flatpak install flathub org.freedesktop.Platform//21.08 org.freedesktop.Sdk//21.08
	flatpak install flathub org.freedesktop.Sdk.Extension.openjdk8//21.08

## Adding a remote repo where repo folder was uploaded and installing
	flatpak remote-add  hirs https://website.com/flatpak-repo/ --no-gpg-verify
	sudo flatpak install org.hirs.aca

	Note: sudo flatpak install is needede if no gpg key was created

## Installing from build bundle
	flatpak install org.hirs.aca



	
