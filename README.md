Amazon Product Advertising API Client extension
==========

This project contains additional classes in order to start working with the source code, generated from the official SOAP webservice.

Most of the **com.ECS.client** package is a product of using **wsimport** against [AWSECommerceService](https://webservices.amazon.com/AWSECommerceService/AWSECommerceService.wsdl)

The only different from the generated **AWSECommerceService.java** is the following code which is used to add additional logic to the outgoing SOAP calls (e.g. security signature):

```java
@HandlerChain(file="handler-chain.xml")
```

### Requirements

- JDK 8 (AWSHandler could easily be ported to JDK 6)
- SLF4J and Logback

-----------
### Usage

1. Configure maven dependency
```xml
<repositories>
  <repository>
    <id>bintray</id>
    <url>http://dl.bintray.com/rchukh/maven</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.rchukh.amazon</groupId>
    <artifactId>paapi</artifactId>
    <version>0.1</version>
  </dependency>
</dependencies>
```

2. Add **amazon.properties** to the ClassPath with the following content(required vbalues should be provided before usage):

```
# Access Key (required)
amazon.accessKeyId=
# Secret Key (required)
amazon.secretKeyId=
# Default associate tag (not required)
amazon.paapi.defaultAssociateTag=
```

-----------
### Example:

```java
import com.ECS.client.jax.*;
import com.github.amazon.paapi.AWSProps;

public class AWSTest {

	public static void main(String[] args) {
		AWSECommerceService commerceService = new AWSECommerceService();
		AWSECommerceServicePortType portUS = commerceService.getAWSECommerceServicePortUS();

		ItemSearchRequest itemSearchRequest = new ItemSearchRequest();
		// Fill in the request object:
		itemSearchRequest.setSearchIndex("Books");
		itemSearchRequest.setKeywords("amazon");

		ItemSearch itemSearch = new ItemSearch();
		itemSearch.setAWSAccessKeyId(AWSProps.INSTANCE.getAccessKeyId());
		itemSearch.setAssociateTag(AWSProps.INSTANCE.getDefaultAssociateTag());
		itemSearch.getRequest().add(itemSearchRequest);

		ItemSearchResponse itemSearchResponse = portUS.itemSearch(itemSearch);
		// Do something with response, e.g. print a total number of results
		System.out.println(itemSearchResponse.getItems().get(0).getTotalResults());
	}
}
```
