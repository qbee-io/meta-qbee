# classes/rauc-distro-meta.bbclass

# Define the namespace globally
RAUC_META_SECTIONS ?= "release_info"

python __anonymous () {
    # CRITICAL: Only run this I/O logic if the recipe inherits bundle.bbclass.
    if bb.data.inherits_class('bundle', d):
        import os
        
        # Expand the global variable to get the absolute path
        changelog_path = d.expand(d.getVar('DISTRO_CHANGELOG_PATH') or "")
        
        if not changelog_path:
            bb.warn("DISTRO_CHANGELOG_PATH is not set.")
            return

        # Tell BitBake to track this file as a dependency
        bb.parse.mark_dependency(d, changelog_path)
        
        # Read and sanitize the file
        if os.path.exists(changelog_path):
            with open(changelog_path, 'r') as f:
                # Replace actual newlines with literal '\n' characters
                changelog_content = f.read().strip().replace('\n', ' \\n ')
        else:
            # fail if the file doesn't exist, since it's critical for the build
            raise FileNotFoundError(f"Changelog file not found at: {changelog_path}")
            
        # Inject the parsed text into the RAUC flag
        d.setVarFlag('RAUC_META_release_info', 'changelog', changelog_content)
}