#https://huggingface.co/stable-diffusion-v1-5/stable-diffusion-v1-5
# python 3.10
# dockerfile python3.10 

import os
import torch
from diffusers import StableDiffusionPipeline
from huggingface_hub import snapshot_download

def generate_studio_ghibli_image(
    prompt: str = "A breathtaking Studio Ghibli style fantasy landscape, soft colors, whimsical atmosphere",
    output_path: str = "studio_ghibli.png",
    num_inference_steps: int = 50,
    model_path: str = "./local_model/stable-diffusion-v1-5"
) -> None:
    """
    Generates an image based on the provided prompt using Stable Diffusion,
    loading the model from a local directory (downloading it if not present),
    and saves it to the specified output path.
    """
    # Download the model files if the local directory does not exist.
    if not os.path.exists(model_path):
        print("Local model not found. Downloading the model...")
        snapshot_download(
            repo_id="runwayml/stable-diffusion-v1-5",
            local_dir=model_path,
            local_dir_use_symlinks=False
        )
    
    # Use GPU if available for faster inference.
    device = "cuda" if torch.cuda.is_available() else "cpu"
    
    # Load the diffusion pipeline from the local directory.
    pipe = StableDiffusionPipeline.from_pretrained(
        model_path,
        local_files_only=True,
        torch_dtype=torch.float16 if device == "cuda" else torch.float32
    )
    pipe = pipe.to(device)
    
    # Generate the image.
    result = pipe(prompt, num_inference_steps=num_inference_steps)
    image = result.images[0]
    
    # Save the generated image.
    image.save(output_path)
    print(f"Image successfully saved to {output_path}")

if __name__ == "__main__":
    prompt_text = "A breathtaking Studio Ghibli style fantasy landscape, soft colors, whimsical atmosphere"
    generate_studio_ghibli_image(prompt=prompt_text)
