package org.jetbrains.dokka

import net.nicoulaj.idea.markdown.lang.ast.*
import net.nicoulaj.idea.markdown.lang.parser.dialects.commonmark.*
import net.nicoulaj.idea.markdown.lang.parser.*
import net.nicoulaj.idea.markdown.lang.*

class MarkdownNode(val node: ASTNode, val markdown: String) {
    val children: List<MarkdownNode> get() = node.children.map { MarkdownNode(it, markdown) }
    val endOffset: Int get() = node.endOffset
    val startOffset: Int get() = node.startOffset
    val type: IElementType get() = node.type
    val text: String get() = markdown.substring(startOffset, endOffset)
    fun child(type: IElementType): MarkdownNode? = children.firstOrNull { it.type == type }
}

fun MarkdownNode.visit(action: (MarkdownNode, () -> Unit) -> Unit) {
    action(this) {
        for (child in children) {
            child.visit(action)
        }
    }
}

public fun MarkdownNode.toTestString(): String {
    val sb = StringBuilder()
    var level = 0
    visit {(node, visitChildren) ->
        sb.append(" ".repeat(level * 2))
        sb.append(node.type.toString())
        sb.append(":" + node.text.replace("\n", "\u23CE"))
        sb.appendln()
        level++
        visitChildren()
        level--
    }
    return sb.toString()
}

public fun MarkdownNode.toHtml(): String {
    val sb = StringBuilder()
    visit {(node, processChildren) ->
        val nodeType = node.type
        val nodeText = node.text
        when (nodeType) {
            MarkdownElementTypes.UNORDERED_LIST -> {
                sb.appendln("<ul>")
                processChildren()
                sb.appendln("</ul>")
            }
            MarkdownElementTypes.ORDERED_LIST -> {
                sb.appendln("<ol>")
                processChildren()
                sb.appendln("</ol>")
            }
            MarkdownElementTypes.LIST_ITEM -> {
                sb.append("<li>")
                processChildren()
                sb.appendln("</li>")
            }
            MarkdownElementTypes.EMPH -> {
                sb.append("<em>")
                processChildren()
                sb.append("</em>")
            }
            MarkdownElementTypes.STRONG -> {
                sb.append("<strong>")
                processChildren()
                sb.append("</strong>")
            }
            MarkdownElementTypes.ATX_1 -> {
                sb.append("<h1>")
                processChildren()
                sb.append("</h1>")
            }
            MarkdownElementTypes.ATX_2 -> {
                sb.append("<h2>")
                processChildren()
                sb.append("</h2>")
            }
            MarkdownElementTypes.ATX_3 -> {
                sb.append("<h3>")
                processChildren()
                sb.append("</h3>")
            }
            MarkdownElementTypes.ATX_4 -> {
                sb.append("<h4>")
                processChildren()
                sb.append("</h4>")
            }
            MarkdownElementTypes.ATX_5 -> {
                sb.append("<h5>")
                processChildren()
                sb.append("</h5>")
            }
            MarkdownElementTypes.ATX_6 -> {
                sb.append("<h6>")
                processChildren()
                sb.append("</h6>")
            }
            MarkdownElementTypes.BLOCK_QUOTE -> {
                sb.append("<blockquote>")
                processChildren()
                sb.append("</blockquote>")
            }
            MarkdownElementTypes.PARAGRAPH -> {
                sb.append("<p>")
                processChildren()
                sb.appendln("</p>")
            }
            MarkdownTokenTypes.CODE -> {
                sb.append("<pre><code>")
                sb.append(nodeText)
                sb.append("</code><pre>")
            }
            MarkdownTokenTypes.TEXT -> {
                sb.append(nodeText)
            }
            else -> {
                processChildren()
            }
        }
    }
    return sb.toString()
}

fun parseMarkdown(markdown: String): MarkdownNode {
    if (markdown.isEmpty())
        return MarkdownNode(LeafASTNode(MarkdownElementTypes.MARKDOWN_FILE, 0, 0), markdown)
    return MarkdownNode(MarkdownParser(CommonMarkMarkerProcessor()).buildMarkdownTreeFromString(markdown), markdown)
}

fun markdownToHtml(markdown: String): String {

    val tree = MarkdownParser(CommonMarkMarkerProcessor()).buildMarkdownTreeFromString(markdown)
    val markdownTree = MarkdownNode(tree, markdown)
    val ast = markdownTree.toTestString()
    return markdownTree.toHtml()
}

