import parser, {j2xParser} from 'fast-xml-parser';

export default class XmlParser
{
	constructor()
	{
		this.options = {
			decode: {
				attributeNamePrefix : '@_',
				attrNodeName: false,
				textNodeName: false,
				ignoreAttributes: false,
				ignoreNameSpace: true,
				allowBooleanAttributes: true,
				parseNodeValue: false,
				parseAttributeValue: false,
				trimValues: true,
				arrayMode: true,
				//attrValueProcessor: (val, attrName) => he.decode(val, {isAttributeValue: true}),//default is a=>a
				//tagValueProcessor : (val, tagName) => he.decode(val), //default is a=>a
			},
			encode: {
				attributeNamePrefix : '@_',
				attrNodeName: false,
				textNodeName: '#text',
				ignoreAttributes: false,
				ignoreNameSpace: false,
				parseNodeValue: false,
				parseAttributeValue: false,
				format: false,
				indentBy: '  ',
				supressEmptyNode: true,
				//tagValueProcessor: (a) => he.encode(a, { useNamedReferences: true}),// default is a=>a
				//attrValueProcessor: (a) => he.encode(a, {isAttributeValue: isAttribute, useNamedReferences: true})// default is a=>a
			}
		};
	}

	fromXml(xml)
	{
		return parser.parse(xml, this.options.decode);
	}

	toXml(json)
	{
		return (new j2xParser(this.options.encode)).parse(json);
	}
}