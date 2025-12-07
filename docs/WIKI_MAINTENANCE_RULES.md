# Updated Wiki Maintenance Rules for Remixed Dungeon

## File Naming Convention

### Standard Format
- All wiki page files must use lowercase names with underscores separating words
- Example: `chaos_armor.txt`, `air_elemental.txt`, `potion_of_healing.txt`
- No capitalized filenames are allowed (e.g., no `Chaos_Armor.txt`, `Air_Elemental.txt`)

### Conversion Strategy
- CamelCase names should be converted to snake_case (e.g., `TheSoulbringer` → `the_soulbringer`)
- Proper names should follow the same rule (e.g., `ArcaneStylus` → `arcane_stylus`)

## Content Organization

### Single Source of Truth
- Each wiki page should have exactly one file in the correct lowercase naming format
- No duplicate content should exist in separate files
- When merging content from duplicate files, combine all information in the lowercase-named file

### Content Structure
- Use consistent heading formats: `====== Page Title ======`
- Organize content with clear sections using `==== Section Title ====`
- Include relevant tags at the bottom: `{{tag> rpd items}}` or `{{tag> rpd mobs}}`

## Content Quality Standards

### Information Accuracy
- All game data (stats, mechanics, effects, drop rates) must be verified against source code
- Include specific numerical values rather than vague descriptions
- When documenting items, include durability, usage restrictions, and exact effects
- For mobs, document HP, damage, special abilities, resistances, and drop tables

### Comprehensive Coverage
- Describe both the mechanical effects and strategic implications of game elements
- Include information about where items/mobs can be found or obtained
- Explain synergies with other items or mechanics when relevant
- Document any special behaviors, AI patterns, or unique mechanics

### Formatting and Style
- Use bullet points for lists of properties, effects, or characteristics
- Include code-style references when mentioning other game elements (e.g., `[[rpd:sword|Sword]]`)
- Present information in order of importance to the player
- Provide examples where helpful for understanding complex mechanics

### Technical Information
- When documenting mechanics from source code, include the relevant class names
- Reference game constants and formulas where they enhance understanding
- Cite string resource names for accuracy of in-game text
- Link to related mechanics or concepts for better context

## Internal Linking Standards

### Link Format
- All internal links must use lowercase page names in the format: `[[rpd:page_name|Display Text]]`
- For same-namespace links, use: `[[page_name|Display Text]]`
- Avoid linking to capitalized file names that no longer exist

### Cross-references
- When linking to related pages, ensure the target page exists in lowercase format
- Use descriptive display text that clarifies the link's purpose
- Maintain consistent terminology across linked pages

## Quality Assurance

### Regular Maintenance
- Run `find_red_links.py` regularly to identify broken links
- Address any red links immediately by creating missing pages or correcting link targets
- Audit for duplicate content and merge as needed

### Content Verification
- Cross-reference all game information with actual game code and string resources
- When documenting mechanics, use information extracted directly from source code
- Update wiki content when game mechanics change in new versions

### Review Process
- Before adding new content, verify that a page doesn't already exist under a different naming convention
- Ensure all new pages follow the lowercase naming standard
- Update related pages to link to new content appropriately

## Migration Guidelines

### For Existing Content
- All capitalized files were merged into their lowercase counterparts during the migration
- Any remaining capitalized files should be removed after confirming content was properly merged
- Links pointing to capitalized file names have been updated to lowercase equivalents

### For New Content
- Create all new pages using the lowercase naming convention
- When creating content that references existing pages, use the correct lowercase link format
- If you find old capitalized links still in code or documentation, update them to lowercase format

## Tools and Automation

### Script Usage
- Use `find_red_links.py` to periodically scan for broken or incorrect links
- Use the merge script to handle any future duplicate files that may be created
- Implement automated checking in development workflow to catch naming convention violations

### Verification Steps
1. Before committing wiki changes, run `find_red_links.py --output red-links`
2. Verify all new links point to existing lowercase files
3. Ensure no capitalized files are being created
4. Check that merged content doesn't introduce duplicate information within pages

## Exceptions and Special Cases

### External Links
- Links to external resources (HTTP/HTTPS) maintain their original format
- Links to special namespaces (wiki: pages, etc.) may follow different conventions

### Images and Assets
- Image references in wiki pages should maintain their original naming if they're external assets
- New images should follow same lowercase convention where possible

## Enforcement

### Responsibilities
- All wiki contributors must follow these naming conventions
- Code reviewers should verify wiki changes adhere to these standards
- Automated checks should be run as part of the build process where possible

### Migration Tracking
- A record of all merged files and their new locations is maintained for historical reference
- Links from outside sources should be updated to reflect the new naming convention