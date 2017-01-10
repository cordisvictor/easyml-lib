# easyml-lib
Repository for the EasyML to and from XML library.

EasyML
- offers basic DTD-type support and full Java support
  (see EasyML and EasyML.Profile configuration).
- Dependencies:
  "kxml2-min-2.3.0.jar" - compile+runtime
  (http://sourceforge.net/projects/kxml/files/kxml2/2.3.0/kxml2-min-2.3.0.jar/download)


!Release 1.4.2
- bugfix: n.s.e.m.j.i.SerializableStrategy GetFieldImpl readFields fix.


!Release 1.4.1
- bugfix: XMLWriter text driver empty line when pretty printing.


!Release 1.4.0
- NON-BACKWARD COMPATIBLE refactor: EasyML now immutable(removed setters).
- feature: EasyMLBuilder for easyml customization.


!Release 1.3.10
- bugfix: XMLWriter text driver empty line when pretty printing.
- javadoc: improvements.


!Release 1.3.9
- feature: EasyML, XMLReader, XMLWriter custom XML root tag setting.
- refactor: source level 1.7 warnings fixed.


!Release 1.3.8
- performance: EasyML cache reflected class constructors.
- feature: EasyML, XMLReader, XMLWriter clearCache() methods.


!Release 1.3.7
- performance: EasyML and XMLReader cache reflected classes and fields.
    This is done via n.s.e.m.UnmarshalContext's classFor() and fieldFor().
    Distinct EasyML instances have separate caches.
    XMLReaders share caches only when isSharedConfiguration().
- feature: added EasyML.Style.FAST.
- bugfix: ClassStrategy did not take into account aliasing settings.
- bugfix: remove duplicate encoded.clear() from XMLWriter.reset().


!Release 1.3.6
- feature: added n.s.e.EasyML.releaseCurrentReader()
    and n.s.e.EasyML.releaseCurrentWriter() methods.
- performance: removed the redundant easyml version attribute.


!Release 1.3.5
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


!Release 1.3.4
- performance: n.s.e.XMLWriterTextDriver improvements allow EasyML to
    perform up to 20% faster. The performance improvement is more
    pronounced when pretty printing is enabled.


!Release 1.3.3
- doc: better javadoc for n.s.e.EasyML, n.s.e.XMLReader, n.s.e.XMLWriter.
- bugfix: include position descriptor in each InvalidFormatException case.
- bugfix: n.s.e.XMLReaderTextDriver.consumeFully() fix.


!Release 1.3.2
- performance: n.s.e.EasyML serialize() and deserialize() methods use
    ThreadLocal internally for XMLWriter and XMLReader instance reuse,
    in order to improve performance, while remaining thread-safe.


!Release 1.3.1
- feature: added n.s.e.EasyML newReader() and newWriter methods in order
    to expose more API features at facade level.
- bugfix: prevent shared-config readers and writers from allowing config
    modifications.


!Release 1.3.0
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


!Release 1.2.6
- feature: fail first security policy enforcement for n.s.e.XMLReader
    readObject and readArray.
- bugfix: security policy now validates params at add and addHierarchy.
- bugfix: consume remaining XML on security policy exception, to allow
    subsequent reads, if possible.
- bugfix: better XMLWriter and XMLReader close method impls.


!Release 1.2.5
- feature: n.s.e.EasyML and n.s.e.XMLReader: new securityPolicy settings
  used to configure, if needed, black- or whitelists for objects found at
  deserialization time.


!Release 1.2.4
- NON-BACKWARD COMPATIBLE refactor: n.s.e.m.CompositeStrategy: removed the
    "defTarget" parameter as it was only used when skipDefaults was true
    and it was buggy in some cases such as a bean setting a property in a
    default sub-bean (composition, not inheritance) within it's default
    constructor.


!Release 1.2.3
- feature: EasyML: added unregisterSimple() and unregisterComposite().


!Release 1.2.2
- refactor: CompositeWriter.endAttributes() now deprecated for simpler
    n.s.e.m.CompositeStrategy.marshal() implementation code. Use
    endElement() for endAttributes(true) and simply remove the
    endAttributes(false) invocations.
- refactor: standard unmarshalling implementations now wrap exceptions
    into InvalidFormatException.


!Release 1.2.1 
- feature: XMLWriter and XMLReader: DTD and NON-DTD primitives are now
    configured to not create confusion between the default configuration
    and the primitives API.
- feature: XMLWriter, XMLReader, and n.s.e.m.j.l.ArrayStrategy: array
    processing is now done with less reflection and no auto-boxing.
- refactor: util package is now simpler and public.
- bugfix: XMLWriter.flush() now sets underlying driver into initial state
    in case of further calls to write().
- bugfix: XMLReader now rechecks for easyml start tag after the easyml
    end tag event.


!Release 1.2.0
- refactor: merged the two jars into a single jar, for simplicity, since
    the resulting easyml-1.2.0.jar is only ~370kB in size. Java plackage
    and class names remain the same.
- feature: XMLWriter and XMLReader: added API for the Java primitive types
    to avoid autoboxing and casting.
- feature: n.s.e.m.j.l.ArrayStrategy: uses the new primitives API for
    arrays of primitives (faster by ~7% for large primitive arrays, for
    example: the internal representation of GSCollections).


!Release 1.1.1
- feature: XMLWriter.write(obj): write merges start-end tags if no value,
    just like n.s.e.m.j.l.ObjectStrategy does.
- bugfix: XMLWriterTextDriver: fixed incorrect indentation of endTags.
    Regression since previous version (1.1.0).
- bugfix: XMLReaderDOMDriver: fixed DOM impl of CompositeAttributeReader:
    should return null instead of "" for absent attributes. 
  Note: indentation bug and start-end-merge enhancement relate to pretty
        printing and do not affect parsing.


!Release 1.1.0
- feature: EasyML now provides API for working with XML already in W3C DOM
    documents form.
- bugfix: XMLReader validation of simple and composite strategy names
    fixed at put().
- Javadoc: improved overall javadoc in package net.sourceforge.easyml.


!Release 1.0.4
- feature: EasyML.deserialize can read multiple objects from same stream
    if deserialize is called with the same input argument.
- feature: defined EasyML.setCustomStringTag and EasyML.setCustomArrayTag
    for customizing outputed XML, for example: "array" containing "string"
    can be customized to "names" containing "name".
- bugfix: n.s.e.m.j.a.ColorStrategy: prevent Color static block from
    executing awt code on some JVMs.


!Release 1.0.3
- feature: added strategies for Stack and TreeMap as part of
    n.s.e.ExtendedEasyML.Profile.GENERIC.
- refactor: n.s.e.StrategyRegistry now an innerclass of n.s.e.XMLWriter.
- refactor: n.s.e.ExtendedEasyML.Profile: LANGUAGE_SPECIFIC renamed to
    SPECIFIC.
- refactor: n.s.e.ExtendedEasyML.Profile: refactored internal
    representation.
- refactor: n.s.e.m.j.u.CollectionStrategy now sets ATTRIBUTE_SIZE so that
    subclasses don't have to.


!Release 1.0.2
- feature: added java.util.Collections.singletonXXX() strategies to
    simplify the EasyML format for these types.
- bugfix: n.s.e.m.j.i.SerializableStrategy: fixed Java IO Serialization
    PutFields/GetFields EasyML impl such that it is not mandatory to
    define both methods when custom serialization of a class is used.
