## Changelog

### 0.11.0
- Kotlin Flow helpers
- Type aliases for diffUtil helper readability

### 0.10.0
- Threading and Consistency checks in the results of ListProvider
- Latest transitive dependencies

### 0.9.0
- Paged list support
- RxListProvider is no more, use a ListProvider along with a DiffUtilTransformer for the same effect. 
`.compose(rxListProvider)` becomes `.diffUtil(listProvider)`

### 0.8.1
- Bug fix with an overloaded constructor not attaching to item provider.

### 0.8.0
- Basic diff util callback factory functions

### 0.7.0
- Add secondary construction option for those time multibinds is just not the right solution.

### 0.6.1
- Synchronize Fix concurrency issue due to the lazy nature of delegate caching.

### 0.6.0 - BREAKING CHANGES
- Module breakup and artifact id changes
- Move adapter configuration entirely into constructor
- Remove androidx list differ provider impl's. Threading is on the consumer now.

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
