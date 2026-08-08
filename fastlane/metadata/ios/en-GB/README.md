Drop App Store screenshots for this locale directly in this folder as flat `.png` files
(no subfolders needed) — `deliver` detects each device size from the image's pixel
dimensions. Apple requires at minimum one 6.9" iPhone set; common sizes:

- 6.9" iPhone (e.g. iPhone 17 Pro Max): 1320 x 2868
- 6.5" iPhone (e.g. iPhone 11 Pro Max): 1284 x 2778
- iPad Pro 13" (if the app supports iPad): 2064 x 2752

Capture from Simulator with `Cmd+S`, then move the files here and rename them something
descriptive (e.g. `01_home.png`, `02_sessionDetails.png` — sort order in the App Store
listing follows filename order).

Once populated, run `fastlane ios uploadScreenshots` from the repo root to publish —
uploads screenshots only, no binary or listing text touched.
