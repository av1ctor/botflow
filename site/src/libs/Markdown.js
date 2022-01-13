import MarkdownIt from 'markdown-it';
import emoji from 'markdown-it-emoji/light';

export default class Markdown
{
	static md = new MarkdownIt({
		html: false,
		linkify: true,
		typographer: false
	}).use(emoji);

	static render(text)
	{
		return this.md.render(text);
	}
}