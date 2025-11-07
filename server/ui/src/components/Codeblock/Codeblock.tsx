import React, { JSX, useEffect, useState } from "react";
import type { PrismTheme, Language } from "prism-react-renderer";
import { Highlight, themes } from "prism-react-renderer";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import clsx from "clsx";
import downloadFile from "js-file-download";
import styles from "./Codeblock.module.css";

type ThemeValue = "light" | "dark";

const getTheme = () =>
	(document.documentElement.dataset.theme || "light") as ThemeValue;

function useGlobalTheme() {
	const [theme, setTheme] = useState<ThemeValue>(getTheme);
	useEffect(() => {
		const mo = new MutationObserver(() => {
			setTheme(getTheme());
		});
		mo.observe(document.documentElement, {
			subtree: false,
			attributeFilter: ["data-theme"],
		});
		return () => mo.disconnect();
	});
	return theme;
}

function Codeblock({
	children,
	language = "markup",
	enableCopy = false,
	download,
}: {
	children: string;
	language?: Language;
	enableCopy?: boolean;
	download?: { fileName: string; mime: string };
}): JSX.Element {
	const { siteConfig } = useDocusaurusContext();
	const theme = useGlobalTheme();
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const codeThemeLight = (siteConfig.themeConfig.prism as any)
		.theme as PrismTheme;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const codeThemeDark = (siteConfig.themeConfig.prism as any)
		.darkTheme as PrismTheme;
	const codeTheme = theme === "light" ? codeThemeLight : codeThemeDark;

	const handleCopy = async () => {
		try {
			navigator.clipboard.writeText(children);
		} catch {
			// Copying did unfortunately not work, but we'll not crash the app
			// beacause of that...
		}
	};
	const handleDownload = () => {
		if (!download || !download.fileName || !download.mime) return;
		downloadFile(children, download.fileName, download.mime);
	};

	return (
		<div className={styles.wrapper}>
			<Highlight
				code={children}
				language={language}
				theme={codeTheme}
			>
				{({ className, style, tokens, getLineProps, getTokenProps }) => (
					<pre className={clsx(className, styles.codeblock)} style={style}>
						{tokens.map((line, i) => (
							// eslint-disable-next-line react/jsx-key
							<div {...getLineProps({ line, key: i })}>
								{line.map((token, key) => (
									// eslint-disable-next-line react/jsx-key
									<span {...getTokenProps({ token, key })} />
								))}
							</div>
						))}
					</pre>
				)}
			</Highlight>
			{(enableCopy || download) && (
				<div className={styles.buttonWrapper}>
					{enableCopy && (
						<button
							className={styles.button}
							type="button"
							aria-label="Copy content"
							title="Copy content"
							onClick={handleCopy}
						>
							<svg aria-hidden="true" viewBox="0 0 24 24" fill="currentColor">
								<path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z" />
							</svg>
						</button>
					)}
					{download && (
						<button
							className={styles.button}
							type="button"
							aria-label="Download content as file"
							title="Download content as file"
							onClick={handleDownload}
						>
							<svg aria-hidden="true" viewBox="0 0 24 24" fill="currentColor">
								<path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z" />
							</svg>
						</button>
					)}
				</div>
			)}
		</div>
	);
}

export default Codeblock;
