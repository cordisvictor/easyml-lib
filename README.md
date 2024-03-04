## EasyML is a Java serialization library, to and from XML, similar to [Gson](http://github.com/google/gson).

EasyML library converts Java objects into XML and back again, without the need for annotations or other
types of configuration. EasyML offers extensive support for JDK classes and also supports customization
through user settings, user extensions, or through the Java Serialization API.

EasyML provides support for:
 * reading from and writing to XML text
 * reading from and writing to org.w3c.dom.Document structures
 * Java Collections framework
 * Java Serialization framework
 * Multi-threading
 * Java Generics


### Example

Creating a default EasyML instance:
```java
EasyML easyml = new EasyML();
```

Creating a custom EasyML instance:
```java
EasyML easyml = new EasyMLBuilder()
            .withStyle(EasyML.Style.PRETTY)
            .withCustomRootTag("persons")
            .withAlias(PersonDTO.class, "person")
            .build();
```
EasyML can also be customized with user-defined serialization strategies.
Security policies can also be defined, specifying black- or whitelists of
types which are allowed at deserialization time.

Serializing:
```java
easyml.serialize(srcObj, outFile);

easyml.serialize(srcObj, outDOM);

String xml= easyml.serialize(srcObj);
```

Deserializing:
```java
Object o= easyml.deserialize(inFile);

Object o= easyml.deserialize(inDOM);

Object o= easyml.deserialize(inStr);
```
The low-level components, XMLWriter and XMLReader, can be used directly,
for a higher control compared to the EasyML Facade.

### Dependencies

  KXml2
  [kxml2-min-2.3.0.jar](http://sourceforge.net/projects/kxml/files/kxml2/2.3.0/kxml2-min-2.3.0.jar/download)


### Release Notes

Release 1.7.1
- feature: added List.of, Map.of, Set.of strategies.
- feature: added java.util.Collections unmodifiableList, unmodifiableMap, unmodifiableSet strategies.


Release 1.7.0 (requires Java 17, recommended from Java 17)
- feature: support for Java records.


Release 1.6.0 (requires Java 9, recommended up to Java 17)
- XMLWriter and XMLReader use getters and setters.
- feature: support for Java 9 modules.
- NON-BACKWARD COMPATIBLE refactor of ReflectionUtil.


Release 1.5.3
- refactor: remove deprecated Class.newInstance() usages.
- refactor: limited reflection usage from Properties, EnumSet, EnumMap,
SingletonList, SingletonSet, SingletonMap strategies.
- feature: added java.util.concurrent.atomic strategies.
- feature: added java.util.Collections emptyList, emptyMap, emptySet strategies.


Release 1.5.2
- feature: added generic mechanism for cache clearing.


Release 1.5.1
- feature: added functional API.
- feature: added CalendarStrategy and OptionalStrategy.
- feature: added java.time strategies.
- performance: added caching to SerializableStrategy.
- refactor: remove Profile feature.


Release 1.5.0 (requires Java 8, recommended up to Java 9)
- feature: support for Java 9 security.
- NON-BACKWARD COMPATIBLE refactor of ReflectionUtil.


Release 1.4.7
- last Java 7 compatible release.
- bugfix: performance regression in Serialization strategy.
- remove AccessibleObject.isAccessible calls.


Release 1.4.6
- remove deprecated v1.3.4 object-o and array-o strategies.
- replaced MarshalContext aliasFor methods with aliasOrNameFor methods.
- bugfix: EnumStrategy marshalling fix.
- feature: added TeeSet and ConcurrentHashMap support.
- feature: added EnumSet and EnumMap support.
- feature: added serialization serialPersistentFields support.


Release 1.4.5
- performance: improved n.s.e.EasyML.deserialize() speed by reusing the
XmlPullParser when available.
- refactor: minor code improvements.


Release 1.4.4
- refactor: made n.s.e.m.CompositeStrategy.unmarshalInit return type more
loose to better support readResolve in n.s.e.m.j.i.ExternalizableStrategy.
- refactor: n.s.e.XMLReader and n.s.e.XMLWriter use HashMap instead of
ConcurrentHashMap if not in shared mode (i.e. if in standalone mode).


Release 1.4.3
- feature: new n.s.e.m.j.i.ExternalizableStrategy offers support for the
Java Externalizable protocol.
- feature: new n.s.e.XMLReader.hasMore method.
- feature: new n.s.e.m.j.u.BitSetStrategy available and included into the
EasyML.Profile.Generic for more portable XML.
- performance: n.s.e.m.j.i.SerializableStrategy prevent auto-boxing in
object input and output streams.


Release 1.4.2
- bugfix: n.s.e.m.j.i.SerializableStrategy GetFieldImpl readFields fix.


Release 1.4.1
- bugfix: XMLWriter text driver empty line when pretty printing.


Release 1.4.0
- NON-BACKWARD COMPATIBLE refactor: EasyML now immutable(removed setters).
- feature: EasyMLBuilder for easyml customization.


Release 1.3.11
- bugfix: n.s.e.m.j.i.SerializableStrategy GetFieldImpl readFields fix.
- bugfix: XMLWriter text driver startElement impl improvement.


Release 1.3.10
- bugfix: XMLWriter text driver empty line when pretty printing.
- javadoc: improvements.


Release 1.3.9
- feature: EasyML, XMLReader, XMLWriter custom XML root tag setting.
- refactor: source level 1.7 warnings fixed.


Release 1.3.8
- performance: EasyML cache reflected class constructors.
- feature: EasyML, XMLReader, XMLWriter clearCache() methods.


Release 1.3.7
- performance: EasyML and XMLReader cache reflected classes and fields.
This is done via n.s.e.m.UnmarshalContext's classFor() and fieldFor().
Distinct EasyML instances have separate caches.
XMLReaders share caches only when isSharedConfiguration().
- feature: added EasyML.Style.FAST.
- bugfix: ClassStrategy did not take into account aliasing settings.
- bugfix: remove duplicate encoded.clear() from XMLWriter.reset().


Release 1.3.6
- feature: added n.s.e.EasyML.releaseCurrentReader()
    and n.s.e.EasyML.releaseCurrentWriter() methods.
- performance: removed the redundant easyml version attribute.


Release 1.3.5
- performance: reduced the impact of class aliasing and field aliasing
features, even when NOT used, on serialize() and deserialize() times.
- performance: n.s.e.u.ReflectionUtil unsafe instantiation method now
ensures a single runtime-dependent instantiator instance.
- refactor: n.s.e.m.MarshalContext and n.s.e.m.UnmarshalContext getter
for simpleDateFormat changed to formatDate() and parseDate() for
security reasons.
- refactor: n.s.e.m.CompositeWriter.endAttributes() was removed since
it was deprecated since version 1.2.2.
- refactor: renamed "object-o", "array-o" to "objectx", "arrayx"
- bugfix: object field values could get inverted at read if ALL of the
    following conditions hold:
    1. skipDefaults is enabled.
    2. instance has same name fields on different inheritance levels.
    3. instance defines default values on each level.
    4. subfield has default value, while superfield as non-default value.
    5. superfield is the 1st XML field of it's declaring class.
    6. instance is not serializable.
- BACKWARD COMPATIBLE: version 1.3.5 is configured to be compatible with
XML outputted by versions 1.3.5 down to 1.2.1 and will produce only
1.3.5 formatted XML.


Release 1.3.4
- performance: n.s.e.XMLWriterTextDriver improvements allow EasyML to
    perform up to 20% faster. The performance improvement is more
    pronounced when pretty printing is enabled.


Release 1.3.3
- doc: better javadoc for n.s.e.EasyML, n.s.e.XMLReader, n.s.e.XMLWriter.
- bugfix: include position descriptor in each InvalidFormatException case.
- bugfix: n.s.e.XMLReaderTextDriver.consumeFully() fix.


Release 1.3.2
- performance: n.s.e.EasyML serialize() and deserialize() methods use
ThreadLocal internally for XMLWriter and XMLReader instance reuse,
in order to improve performance, while remaining thread-safe.


Release 1.3.1
- feature: added n.s.e.EasyML newReader() and newWriter methods in order
to expose more API features at facade level.
- bugfix: prevent shared-config readers and writers from allowing config
modifications.


Release 1.3.0
- NON-BACKWARD COMPATIBLE refactor:
merged n.s.e.ExtendedEasyML into n.s.e.EasyML.
- feature: n.s.e.EasyML facade is now thread-safe while the thread-unsafe
n.s.e.XMLReader and n.s.e.XMLWriter offer prototyping for faster per-
thread copy instance creation.
- feature: marshal- and unmarshal contexts are dateformat-aware.
- revert: better XMLWriter and XMLReader close method impls because of
multithread features.
- bugfix: setCustomArrayTag validation fixed.
Note: except for the last mentioned bugfix, all other changes were made
      to offer an easier and faster API for multithreaded environments.

