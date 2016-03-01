/*
 * An XML document type.
 * Localname: restriction
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RestrictionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one restriction(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RestrictionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RestrictionDocument
{
    private static final long serialVersionUID = 1L;
    
    public RestrictionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RESTRICTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "restriction");
    
    
    /**
     * Gets the "restriction" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RESTRICTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "restriction" element
     */
    public void setRestriction(gov.loc.premis.v3.StringPlusAuthority restriction)
    {
        generatedSetterHelperImpl(restriction, RESTRICTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "restriction" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RESTRICTION$0);
            return target;
        }
    }
}
