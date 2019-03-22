## Changelog

### 0.6.0
- Move adapter configuration entirely into constructor
- Relocate specific item providers into separate modules
- Remove androidx list differ impl's
TODO(readme updates)
TODO(publishing configuration updates)

### 0.5.0
- Optional RxAndroid Dependency to provide an impl of an RX based diff processing list provider
- Rename the existing providers, not everything needs to be poly. Breaking Change, Migration provided via deprecation.
- Moved extension functions to live with the provider that they belong to. Breaking Change, Migration provided by IDE import prompt & optimize imports.

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