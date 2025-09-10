/* eslint-disable react/jsx-props-no-spreading */
import React, { JSX } from "react";
import clsx from "clsx";
import type { DropEvent } from "react-dropzone";
import { useDropzone } from "react-dropzone";
import type { DropzoneProps, RejectionType } from "./types";
import styles from "./Dropzone.module.css";

const Dropzone = ({
	accept,
	children,
	className,
	activeClassName,
	multiple = false,
	name,
	onDrop,
	hasSelectedFiles,
	...props
}: DropzoneProps): JSX.Element => {
	const handleDrop = (
		accepted: File[],
		fileRejections: RejectionType[],
		event: DropEvent,
	) => {
		const rejected = fileRejections.map((rejection) => rejection.file);
		onDrop(accepted, rejected, event);
	};
	const {
		getRootProps,
		getInputProps,
		isDragActive,
		isDragAccept,
		isDragReject,
	} = useDropzone({ accept, multiple, onDrop: handleDrop, ...props });
	return (
		<div
			{...getRootProps()}
			className={clsx(
				styles.dropzone,
				isDragActive && styles.active,
				hasSelectedFiles && styles.hasFiles,
				className,
				isDragActive && activeClassName,
			)}
			data-testid="dropzone"
			data-is-drag-active={isDragActive}
			data-is-drag-accepted={isDragAccept}
			data-is-drag-rejected={isDragReject}
			data-has-files={hasSelectedFiles}
		>
			<div
				className={clsx(
					styles.fileHoverPreview,
					isDragActive && styles.previewActive,
				)}
			>
				<svg
					className={clsx(styles.icon, styles.fileHoverIcon)}
					fill="currentColor"
					aria-hidden="true"
					viewBox="0 0 24 24"
				>
					<path d="M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13z" />
				</svg>
			</div>
			<svg
				className={clsx(styles.icon, styles.uploadIcon)}
				fill="currentColor"
				aria-hidden="true"
				viewBox="0 0 24 24"
			>
				<path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z" />
			</svg>
			{children}
			<input name={name} {...getInputProps()} data-testid="dropzone-input" />
		</div>
	);
};

export default Dropzone;
