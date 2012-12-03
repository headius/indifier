indifier: a tool for transforming JVM bytecode to use invokedynamic.

The goal here is to provide a utility for converting exsting JVM
bytecode to use invokedynamic rather than pre-Java 7 mechanisms
for calling methods, accessing fields, etc.

The initial use case I have in mind is for JRuby to transform all
dyncalls from Java to Ruby into invokedynamic calls, so they can be
done in an inlinable, optimizable way. Other use cases would be
modifying Java code to treat calculated static final values as
actually constant, hooking into method calls, field accesses, and
array accesses, and implementing simple dynamic invocation with
normal Java code.
