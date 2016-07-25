<?xml version="1.0"?>

<!-- Convert a WHOIS v3 classes.xml file to a comma separated list of Tuple<>s -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<!-- Good times style case conversion; also translates - to _ -->
<xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz-'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ_'" />

<!-- Get the spacing exact if spurious newlines aren't your cup o' microwave cake -->
<xsl:template match="/">
    <xsl:for-each select="//ripe_class">            new Tuple&lt;&gt;(<xsl:value-of select="dbase_code/@value"/>, ObjectType.<xsl:value-of select="translate(@name, $smallcase, $uppercase)"/>),
</xsl:for-each>
</xsl:template>

</xsl:stylesheet>

<!-- This can be run using xsltproc from libxslt -->
