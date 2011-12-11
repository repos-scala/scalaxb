import org.specs2._

object ProtocolSpec extends Specification { def is =
  "this is a specification to check the generated protocol source"            ^
                                                                              p^
  "the generated protocol source should"                                      ^
    "start with // Generated by"                                              ! protocol1^
                                                                              end^
  "top-level complex types should"                                            ^
    "generate a format typeclass instance"                                    ! complexType1^
    "generate a combinator parser"                                            ! parser1^
    "generate an XML writer"                                                  ! output1^
    "be referenced as Option[A] in the parser if nillable"                    ! cardinality1
                                                                              end

  import Example._
  lazy val module = new scalaxb.compiler.xsd2.Driver
  lazy val emptyProtocol = module.processNode(<xs:schema targetNamespace="http://www.example.com/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" />, "example")(1)

  def protocol1 = {
    println(emptyProtocol)
    emptyProtocol must startWith("// Generated by")
  }

  lazy val addressProtocol = module.processNode(<xs:schema targetNamespace="http://www.example.com/general"
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      xmlns:gen="http://www.example.com/general"
      elementFormDefault="qualified">
    <xs:complexType name="Address">
      <xs:sequence>
        <xs:element name="street" type="xs:string"/>
        <xs:element name="city" type="xs:string"/>
      </xs:sequence>
    </xs:complexType>
  </xs:schema>, "example")(1)

  def complexType1 = {
    println(addressProtocol)
    (addressProtocol must contain(
      """implicit lazy val ExampleAddressFormat: scalaxb.XMLFormat[example.Address] = new DefaultExampleAddressFormat {}""")) and
    (addressProtocol must contain(
      """trait DefaultExampleAddressFormat extends scalaxb.ElemNameParser[example.Address] {"""))
  }

  def parser1 = {
    (addressProtocol must contain(
      """(scalaxb.ElemName(Some("http://www.example.com/general"), "street")) ~""")) and
    (addressProtocol must contain(
      """(scalaxb.ElemName(Some("http://www.example.com/general"), "city"))""")) and
    (addressProtocol must contain(
      """{ case p1 ~ p2 =>""")) and
    (addressProtocol must contain(
      """example.Address(scalaxb.fromXML[String](p1, scalaxb.ElemName(node) :: stack),"""))
  }

  def output1 = {
    (addressProtocol must contain(
      """def writesChildNodes(__obj: example.Address, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =""")) and
    (addressProtocol must contain(
      """Seq.concat(scalaxb.toXML[String](__obj.street, Some("http://www.example.com/general"), Some("street"), __scope, false),"""))
  }

  lazy val cardinalityProtocol = module.processNode(complexTypeCardinalityXML, "example")(1)

  def cardinality1 = {
    println(cardinalityProtocol)
    (cardinalityProtocol must contain(
      """scalaxb.toXML[Option[example.Person]](__obj.person2, None, Some("person2"), __scope, false)"""))
  }
}
