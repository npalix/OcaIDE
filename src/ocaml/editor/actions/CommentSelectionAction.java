package ocaml.editor.actions;

import ocaml.OcamlPlugin;
import ocaml.editors.OcamlEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.editors.text.TextEditor;

/** This action comments or uncomments the selection in the current editor. */
public class CommentSelectionAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void run(IAction action) {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorPart editorPart = page.getActiveEditor();
			if (editorPart != null) {
				if (editorPart instanceof TextEditor) {
					TextEditor editor = (TextEditor) editorPart;

					TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();

					int selStart = selection.getOffset();
					int selEnd = selStart + selection.getLength();

					// the last selected character can be a newline
					if (selEnd > 1)
						selEnd--;

					IEditorInput input = editor.getEditorInput();
					IDocument document = editor.getDocumentProvider().getDocument(input);

					int startOffset;
					int endOffset;
					try {
						int startLine = document.getLineOfOffset(selStart);
						int endLine = document.getLineOfOffset(selEnd);

						startOffset = document.getLineOffset(startLine);
						IRegion endLineInfo = document.getLineInformation(endLine);
						endOffset = endLineInfo.getOffset() + endLineInfo.getLength();

						String result = switchComment(document.get(startOffset, endOffset - startOffset));
						document.replace(startOffset, endOffset - startOffset, result);

						// reselect the initially selected lines
						startOffset = document.getLineOffset(startLine);
						endLineInfo = document.getLineInformation(endLine);
						endOffset = endLineInfo.getOffset() + endLineInfo.getLength();

						TextSelection sel = new TextSelection(startOffset, endOffset - startOffset);
						editor.getSelectionProvider().setSelection(sel);

					} catch (BadLocationException e) {
						OcamlPlugin.logError("Wrong offset", e);
						return;
					}

				} else
					OcamlPlugin.logError("CommentSelectionAction: only works on ml and mli files");

			} else
				OcamlPlugin.logError("CommentSelectionAction: editorPart is null");
		} else
			OcamlPlugin.logError("CommentSelectionAction: page is null");

	}

	private String switchComment(String input) {

		final int tabSize = OcamlEditor.getTabSize();

		// split the string into lines
		String[] lines = input.split("\\r?\\n");

		// uncomment
		if (isCommented(lines)) {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				lines[i] = unComment(line);
			}
		}
		// comment
		else {
			// find the longest line
			int longest = 0;
			for (String line : lines) {
				int length = calculateLength(line, tabSize);
				if (length > longest)
					longest = length;
			}

			// comment all the lines
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				lines[i] = comment(line, longest, tabSize);
			}
		}

		// rebuild a string from the lines
		StringBuilder builder = new StringBuilder();

		for (String line : lines)
			builder.append(line + "\n");

		// remove the last newline
		builder.setLength(builder.length() - 1);

		return builder.toString();
	}

	private int calculateLength(String line, int tabSize) {
		int length = 0;
		for (int i = 0; i < line.length(); i++)
			if (line.charAt(i) == '\t')
				length += tabSize;
			else
				length++;

		return length;
	}

	/** Return <code>line</code> commented so that it measures <code>length</code> characters */
	private String comment(String line, int length, int tabSize) {

		StringBuilder builder = new StringBuilder();

		builder.append("(*");
		builder.append(line);

		int trailingSpaces = length - calculateLength(line, tabSize);

		for (int i = 0; i < trailingSpaces; i++)
			builder.append(" ");

		builder.append("*)");

		return builder.toString();

	}

	/** Uncomment this line if it is commented */
	private String unComment(String line) {
		line = line.trim();
		if (!isCommented(line))
			return line;

		return trimEnd(line.substring(2, line.length() - 2));
	}

	/** Remove the terminating blank space from this line and return the result */
	private String trimEnd(String str) {
		char[] chars = str.toCharArray();

		int length = chars.length;

		while (length > 0 && chars[length - 1] <= ' ') {
			length--;
		}

		return str.substring(0, length);
	}

	/** Are all the lines commented? */
	private boolean isCommented(String[] lines) {

		for (String line : lines) {
			if (!isCommented(line.trim()))
				return false;
		}

		return true;
	}

	private boolean isCommented(String line) {
		return line.startsWith("(*") && line.endsWith("*)");
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
