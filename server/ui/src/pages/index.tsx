import React, { JSX } from "react";
import Upload from "../components/Upload";
import PageLayout from "../components/PageLayout";

export default function Home(): JSX.Element {
	return (
		<PageLayout
			layoutDescription="KoSIT Validator Daemon"
			headline="Try the validator!"
			description="Upload an XML file here to validate its contents. Note: this is just a demo implementation, not meant for production usage. If you need a production ready implementation you are welcome to contribute to the open source project."
		>
			<Upload />
		</PageLayout>
	);
}
