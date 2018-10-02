## Changelog

### 0.4.0
- Api 28 target
- Androidx migration
- Removal of Concat provider. The delegation friendly api makes this a detail the core library need not carry.
- Removal of exec pools, use the support lib async differs so we can keep this lib as simple as possible
- Revert publishing until I can make the jcenter variant support better

### 0.3.0

- Improved proguard configuration
- Optional callback to signal immediately after list diffs have been dispatched to adapter.
- Variant aware publishing via gradle .module metadata

### 0.2.0

- Initial consumer proguard
- Make item provider API more composition friendly

### 0.1.0

- Initial implementation
- API is pretty much not going to change from here but not going 1.0 in case it does need a change