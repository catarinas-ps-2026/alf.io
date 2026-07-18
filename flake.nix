{
  description = "A multi-platform dev shell flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-26.05";
    nixpkgs-unstable.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      nixpkgs,
      nixpkgs-unstable,
      flake-utils,
      ...
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
        };
        unstable = import nixpkgs-unstable {
          inherit system;
          config.allowUnfree = true;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          LD_LIBRARY_PATH =
            with pkgs;
            lib.makeLibraryPath [
              stdenv.cc.cc
              zlib
              glib
              libxcb
              libglvnd
            ];

          packages = pkgs.lib.flatten [
            (with pkgs; [
              jdk17
              jdt-language-server
              gradle
              pnpm
              nodejs
              postgresql
              mermaid-cli
              plantuml
              uv
              playwright-driver.browsers
              k6
            ])
            (with unstable; [
              typst
              tinymist
              typstyle
            ])
          ];

          buildInputs = [ pkgs.bashInteractive ];

          env = {
            PLAYWRIGHT_BROWSERS_PATH = "${pkgs.playwright-driver.browsers}";
            PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS = "true";
          };
        };
      }
    );
}
