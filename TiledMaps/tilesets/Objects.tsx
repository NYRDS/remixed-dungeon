<?xml version="1.0" encoding="UTF-8"?>
<tileset name="Objects" tilewidth="16" tileheight="16" tilecount="256" columns="16">
 <properties>
  <property name="kind" value="Trap"/>
 </properties>
 <image source="../Png/Objects.png" width="256" height="256"/>
 <tile id="32">
  <properties>
   <property name="kind" value="ConcreteBlock"/>
  </properties>
 </tile>
 <tile id="33">
  <properties>
   <property name="kind" value="Sign"/>
  </properties>
 </tile>
 <tile id="34">
  <properties>
   <property name="kind" value="Deco"/>
   <property name="object_desc" value="candle"/>
  </properties>
 </tile>
 <tile id="36">
  <properties>
   <property name="kind" value="Barrel"/>
  </properties>
 </tile>
 <tile id="75">
  <properties>
   <property name="kind" value="LibraryBook"/>
  </properties>
 </tile>
 <tile id="176">
  <properties>
   <property name="kind" value="Trap"/>
   <property name="script" value="scripts/traps/CutScene"/>
   <property name="trapKind" value="scriptFile"/>
   <property name="uses" type="int" value="-1"/>
  </properties>
 </tile>
</tileset>
