package com.microsoft.model;

//  for including guides with toc items
// [items: [name: Overview,
//          href: index.md,
//          name: Version history,
//          href: history.md,
//          name: name,
//          uid: package.name,...]]
public class Guide {
    private final String name;
    private final String href;

    public Guide(String name, String href) {
        this.name = name;
        this.href = href;
    }

    public String getName() { return name; }
    public String getHref() { return href; }
}