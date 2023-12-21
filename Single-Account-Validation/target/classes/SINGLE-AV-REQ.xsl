<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:pfms="http://pfms.com/AccountValidationRequest" exclude-result-prefixes="pfms">
<xsl:output method="text"/>
<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>
<xsl:template match="/">
<xsl:for-each select="/pfms:Accounts/pfms:Account"><xsl:text/><xsl:value-of select="../@MessageId"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="../@Source"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="../@Destination"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="../@BankCode"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="../@BankName"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="../@RecordsCount"/>|<xsl:text/>
<xsl:text/>http://pfms.com/AccountValidationRequest|<xsl:text/>
<xsl:text/><xsl:value-of select="pfms:AccountNumber"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="pfms:EntityCode"/>|<xsl:text/>
<xsl:text/><xsl:value-of select="pfms:DataRequired"/>;<xsl:text/>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>