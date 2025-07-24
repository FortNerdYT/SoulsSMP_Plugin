# Soul Plugin Resource Pack

This resource pack provides custom textures for all soul types in the Soul Plugin.

## Installation

### For Server Owners:

1. **Upload the resource pack:**
   - Zip the entire `resource-pack` folder
   - Upload the zip file to a file hosting service (GitHub releases, Dropbox, etc.)
   - Get the direct download URL

2. **Configure the plugin:**
   - Edit `plugins/SoulPlugin/config.yml`
   - Set the `resource-pack.url` to your download URL
   - Optionally set the SHA-1 hash for verification
   - Set `force: true` if you want to require the resource pack

3. **Example configuration:**
   ```yaml
   resource-pack:
     url: "https://github.com/yourname/soul-plugin-resourcepack/releases/download/v1.0/soul-plugin-resourcepack.zip"
     hash: "your-sha1-hash-here"
     force: false
   ```

### For Players:

The resource pack will be automatically sent when you join the server. You can also manually install it by:

1. Download the resource pack zip file
2. Place it in your `.minecraft/resourcepacks` folder
3. Enable it in Minecraft's Resource Packs menu

## Texture Files

Place your custom soul textures in the following location:
`resource-pack/assets/minecraft/textures/item/souls/`

### Required texture files:
- `strength_soul.png` (16x16)
- `mace_soul.png` (16x16)
- `sea_soul.png` (16x16)
- `resistance_soul.png` (16x16)
- `regeneration_soul.png` (16x16)
- `dash_soul.png` (16x16)
- `absorption_soul.png` (16x16)
- `haste_soul.png` (16x16)
- `frost_soul.png` (16x16)
- `vampire_soul.png` (16x16)
- `phantom_soul.png` (16x16)
- `saturation_soul.png` (16x16)
- `ender_dragon_soul.png` (16x16)
- `wither_soul.png` (16x16)
- `warden_soul.png` (16x16)

## Custom Model Data Values

Each soul type uses a unique custom model data value:

### Regular Souls (1000-1999):
- Strength Soul: 1001
- Mace Soul: 1002
- Sea Soul: 1003
- Resistance Soul: 1004
- Regeneration Soul: 1005
- Dash Soul: 1006
- Absorption Soul: 1007
- Haste Soul: 1008
- Frost Soul: 1009
- Vampire Soul: 1010
- Phantom Soul: 1011
- Saturation Soul: 1012

### Event Souls (2000-2999):
- Ender Dragon Soul: 2001
- Wither Soul: 2002
- Warden Soul: 2003

## Commands

- `/souls resourcepack <player>` - Send the resource pack to a specific player
- `/souls reload` - Reload the plugin configuration (including resource pack settings)

## Troubleshooting

1. **Resource pack not loading:**
   - Check that the URL is accessible
   - Verify the zip file is properly formatted
   - Check server console for errors

2. **Textures not showing:**
   - Ensure texture files are in the correct location
   - Check that file names match exactly (case-sensitive)
   - Verify the pack format is correct for your Minecraft version

3. **Players can't download:**
   - Make sure the URL is a direct download link
   - Check file hosting service allows direct downloads
   - Verify the file size isn't too large (recommended < 50MB)