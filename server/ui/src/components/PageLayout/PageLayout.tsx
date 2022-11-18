import type { ReactNode } from "react";
import React from "react";
import Layout from "@theme/Layout";
import styles from "./PageLayout.module.css";

function PageLayout({
	children,
	layoutDescription,
	description,
	title,
	headline,
}: {
	children: ReactNode;
	layoutDescription: string;
	description: string;
	headline: string;
	title?: string;
}): JSX.Element {
	return (
		<Layout description={layoutDescription} title={title}>
			<main className="container padding-top--md padding-bottom--lg">
				<h1 className={styles.headline}>{headline}</h1>
				<p>{description}</p>
				{children}
			</main>
		</Layout>
	);
}

export default PageLayout;
