## JavaDoc Doclet for DocFX for Google

Designed to generate YAML files from Javadoc output for DocFX consumption. While the doclet helps output docfx yml,
there are some differences in the way Google needs this outputed to align with the other languages and look appropriate
on devsite.

The original github repository for the doclet can be found
at [github.com/docascode/docfx-doclet](https://github.com/docascode/docfx-doclet).

This doclet is using release [143274](https://github.com/docascode/docfx-doclet/releases/tag/143274) from June 2020 as
it is still compatible with DocFX V2.

\
Changes

- Sort toc by uid
- Included `projectName` argument 
  - adds product level hierarchy to toc
  - adds guides: Overview, Version history to toc 
- Updated summary generation

\
\
This is not an officially supported Google product.
