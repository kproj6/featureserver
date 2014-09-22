Developer notes
===============

General philosophy / architecture
---------------------------------

The resources should only deal with taking input from the end user (i.e. the rest call) and 
producing the output (json). It should not have to do work like talking to files or doing a lot 
of calculation or conversions. We have a NetCdfManager that does this kind of work.


On `final` everywhere
---------------------
Yes it's ugly. But this is like using `const` everywhere in C++. In real code you do it.