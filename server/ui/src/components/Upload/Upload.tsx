import type { FormEventHandler } from "react";
import React, { useCallback, useState } from "react";
import clsx from "clsx";
import Dropzone from "../Dropzone";
import Codeblock from "../Codeblock";
import ErrorDisplay from "../ErrorDisplay";
import useRequest, { RequestStatus } from "../util/useRequest";
import Button from "../Button";
import styles from "./Upload.module.css";

const ENDPOINT = "/";

const ACCEPT = {
	"text/xml": [".xml", ".XML"],
	"application/xml": [".xml", ".XML"],
};

function createFileName(selectedFileName: string | undefined) {
	return selectedFileName
		? `${selectedFileName.replace(/\.xml$/i, "")}-report.xml`
		: "report.xml";
}

function Upload(): JSX.Element {
	const [selectedFile, setSelectedFile] = useState<File | null>(null);
	const [rejected, setRejected] = useState<File[]>([]);
	const { data, error, request, status } = useRequest();

	const handleDrop = useCallback(
		(acceptedFiles: File[], rejectedFiles: File[]) => {
			if (acceptedFiles.length) {
				setSelectedFile(acceptedFiles[0]);
				setRejected([]);
			} else {
				setRejected(rejectedFiles);
			}
		},
		[],
	);

	const handleSubmit: FormEventHandler<HTMLFormElement> = (e) => {
		e.preventDefault();
		if (!selectedFile) return;
		request(ENDPOINT, {
			method: "POST",
			headers: { "Content-Type": "application/xml" },
			body: selectedFile,
			redirect: "follow",
		});
	};

	const meaningfulErrorResponse = !!error && [406, 422].includes(error.code);

	return (
		<>
			<form onSubmit={handleSubmit}>
				{status === RequestStatus.Failure && error && !meaningfulErrorResponse && (
					<ErrorDisplay title="An error occurred while validating the file">
						<Codeblock enableCopy>{error.message}</Codeblock>
					</ErrorDisplay>
				)}
				{rejected.length > 1 && (
					<ErrorDisplay title="Please select a single file only" />
				)}
				{rejected.length === 1 && (
					<ErrorDisplay title="Only XML files are supported">
						<Codeblock>{`Invalid file found: ${rejected[0].name}`}</Codeblock>
					</ErrorDisplay>
				)}
				<Dropzone
					onDrop={handleDrop}
					accept={ACCEPT}
					multiple={false}
					hasSelectedFiles={!!selectedFile}
				>
					{selectedFile ? (
						selectedFile.name
					) : (
						<>Drag &amp; drop files here or click to select a file</>
					)}
				</Dropzone>
				<div className={styles.buttonGroup}>
					<Button
						type="submit"
						disabled={!selectedFile}
						loading={status === RequestStatus.Loading}
					>
						Validate
					</Button>
				</div>
			</form>
			{((data && status === RequestStatus.Success) ||
				meaningfulErrorResponse) && (
				<div
					className={clsx(
						styles.resultDisplay,
						meaningfulErrorResponse && styles.withError,
					)}
				>
					<Codeblock
						download={{
							fileName: createFileName(selectedFile?.name),
							mime: "application/xml",
						}}
						enableCopy
					>
						{data || error?.message || ""}
					</Codeblock>
				</div>
			)}
		</>
	);
}

export default Upload;
