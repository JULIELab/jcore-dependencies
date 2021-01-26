/**
 * <p>
 *     <h1>Binary Representation of JeDIS XMI Annotation Modules</h1>
 *     The main goal of this package is a much more efficient representation of the base document and annotation modules derived
 *     from complete XMI documents. The workflow is as follows:
 *     <h2>Module Generation and Storage</h2>
 *     <ol>
 *         <li>Create annotation modules with an implementation of {@link de.julielab.xml.XmiSplitter}.</li>
 *         <li>Instead of directly storing the annotation modules, encode them into the binary format
 *         using the {@link de.julielab.xml.binary.BinaryJeDISNodeEncoder}.</li>
 *         <li>Store the binary module data.</li>
 *     </ol>
 *     <h2>Module Loading and XMI Document Assembly</h2>
 *     <ol>
 *         <li>Load the base document and the desired set of annotation modules.</li>
 *         <li>Decode the binary format using the {@link de.julielab.xml.binary.BinaryJeDISNodeDecoder}.</li>
 *         <li>Build the final XMI document with the {@link de.julielab.xml.binary.BinaryXmiBuilder}.</li>
 *     </ol>
 * </p>
 */
package de.julielab.xml.binary;