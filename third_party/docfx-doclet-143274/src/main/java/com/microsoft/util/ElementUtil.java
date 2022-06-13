package com.microsoft.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementUtil {

    private static final int INITIAL_CAPACITY = 1000000;
    private final Set<Pattern> excludePackages = new HashSet<>();
    private final Set<Pattern> excludeClasses = new HashSet<>();

    private final Map<Element, List<? extends Element>> elementMap;
    private final Map<Element, List<TypeElement>> elementSortedMap;

    public ElementUtil(String[] excludePackages, String[] excludeClasses) {
        this.excludePackages.addAll(Stream.of(excludePackages)
                .map(o -> Pattern.compile(o)).collect(Collectors.toSet()));
        this.excludeClasses.addAll(Stream.of(excludeClasses)
                .map(o -> Pattern.compile(o)).collect(Collectors.toSet()));
        this.elementMap = new ConcurrentHashMap<>(INITIAL_CAPACITY);
        this.elementSortedMap = new ConcurrentHashMap<>(INITIAL_CAPACITY);
    }

    public List<TypeElement> extractSortedElements(Element element) {
        elementSortedMap.computeIfAbsent(element, this::extractSortedElementsInternal);
        return elementSortedMap.get(element);
    }

    private List<TypeElement> extractSortedElementsInternal(Element element) {
        // Need to apply sorting, because order of result items for Element.getEnclosedElements() depend on JDK implementation
        // By default, exclude private and package-private items
        // todo allow pass parameter for filter items by access modifiers
        return ElementFilter.typesIn(getEnclosedElements(element)).stream()
            .filter(o -> !Utils.isPrivateOrPackagePrivate(o))
            .filter(o -> !matchAnyPattern(excludeClasses, String.valueOf(o.getQualifiedName())))
            .sorted((o1, o2) ->
                StringUtils.compare(String.valueOf(o1.getSimpleName()), String.valueOf(o2.getSimpleName()))
            ).collect(Collectors.toList());
    }

    public List<? extends Element> getEnclosedElements(Element element) {
        elementMap.computeIfAbsent(element, this::getEnclosedElementsInternal);
        return elementMap.get(element);
    }

    private synchronized List<? extends Element> getEnclosedElementsInternal(Element element) {
        return element.getEnclosedElements();
    }

    public List<PackageElement> extractPackageElements(Set<? extends Element> elements) {
        return ElementFilter.packagesIn(elements).stream()
                .filter(o -> !matchAnyPattern(excludePackages, String.valueOf(o)))
                .sorted((o1, o2) ->
                        StringUtils.compare(String.valueOf(o1.getSimpleName()), String.valueOf(o2.getSimpleName()))
                ).collect(Collectors.toList());
    }

    boolean matchAnyPattern(Set<Pattern> patterns, String stringToCheck) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(stringToCheck).matches()) {
                return true;
            }
        }
        return false;
    }
}
