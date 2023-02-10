import type { ReactNode } from "react";
import React from "react";
import styles from "./ErrorDisplay.module.css";

function ErrorDisplay({
	title,
	children,
}: {
	title: string;
	children?: ReactNode;
}): JSX.Element {
	return (
		<div role="alert" className={styles.errorDisplay}>
			<strong className={styles.title}>{title}</strong>
			{children}
		</div>
	);
}

export default ErrorDisplay;
