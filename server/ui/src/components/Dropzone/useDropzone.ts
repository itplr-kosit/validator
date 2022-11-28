import { useCallback, useMemo, useState } from "react";

interface DropzoneHelpers {
	selectedFiles: File[];
	rejectedFiles: File[];
	hasSelectedFiles: boolean;
	getProps: () => {
		onDrop: (accepted: File[], rejected: File[]) => void;
		multiple: boolean;
		accept: string | string[];
		hasSelectedFiles: boolean;
	};
	reset: () => void;
}

function useDropzone({
	multiple = false,
	accept,
}: {
	multiple?: boolean;
	accept: string | string[];
}): DropzoneHelpers {
	const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
	const [rejectedFiles, setRejectedFiles] = useState<File[]>([]);

	const hasSelectedFiles = selectedFiles.length > 0;

	const getProps = useMemo(() => {
		const handleDrop = (accepted: File[], rejected: File[]) => {
			setSelectedFiles(accepted);
			if (rejected.length === 0) {
				setRejectedFiles([]);
			} else {
				setRejectedFiles(rejected);
			}
		};
		return () => ({
			onDrop: handleDrop,
			multiple,
			accept,
			hasSelectedFiles,
		});
	}, [accept, hasSelectedFiles, multiple]);

	const reset = useCallback(() => {
		setSelectedFiles([]);
		setRejectedFiles([]);
	}, []);

	return { selectedFiles, rejectedFiles, hasSelectedFiles, getProps, reset };
}

export default useDropzone;
