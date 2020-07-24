# Cloud Resource Schema
This package defines a unified, serializable schema to represent all cloud resources interesting to Terra. These
cloud resource ids should uniquely identify a resource according to its cloud provider.

It defines the schema in OpenApi format and also generates Java code 
based on the schema. Both OpenApi schema and Java code are in this package. You can either use the Java code directly or 
generated your own code from the OpenApi schema.

Currently, the terra-cloud-resource-lib does not use this directly. It instead uses the client library generated by
the Terra Janitor that includes generated code using this schema.